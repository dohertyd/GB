//
// This Software (the “Software”) is supplied to you by Openmind Networks
// Limited ("Openmind") your use, installation, modification or
// redistribution of this Software constitutes acceptance of this disclaimer.
// If you do not agree with the terms of this disclaimer, please do not use,
// install, modify or redistribute this Software.
//
// TO THE MAXIMUM EXTENT PERMITTED BY LAW, THE SOFTWARE IS PROVIDED ON AN
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE.
//
// Each user of the Software is solely responsible for determining the
// appropriateness of using and distributing the Software and assumes all
// risks associated with use of the Software, including but not limited to
// the risks and costs of Software errors, compliance with applicable laws,
// damage to or loss of data, programs or equipment, and unavailability or
// interruption of operations.
//
// TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW OPENMIND SHALL NOT
// HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, WITHOUT LIMITATION,
// LOST PROFITS, LOSS OF BUSINESS, LOSS OF USE, OR LOSS OF DATA), HOWSOEVER 
// CAUSED UNDER ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
// WAY OUT OF THE USE OR DISTRIBUTION OF THE SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGES.
//

package io.golgi.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.openmindnetworks.golgi.JavaType;
import com.openmindnetworks.golgi.api.GolgiAPI;
import com.openmindnetworks.golgi.api.GolgiAPIHandler;
import com.openmindnetworks.golgi.api.GolgiAPIImpl;
import com.openmindnetworks.golgi.api.GolgiAPINetworkImpl;
import com.openmindnetworks.golgi.api.GolgiException;
import com.openmindnetworks.golgi.api.GolgiTransportOptions;
import com.openmindnetworks.slingshot.ntl.NTL;
import com.openmindnetworks.slingshot.tbx.TBX;

import io.golgi.example.gen.*;
import io.golgi.example.gen.TapTelegraphService.newHiScore;
import io.golgi.example.gen.TapTelegraphService.newPB;
import io.golgi.example.gen.TapTelegraphService.*;


public class Server extends Thread implements GolgiAPIHandler{
    private String devKey = null;
    private String appKey = null;
    private String identity;
    private String paramString;
    private String resultString;
    private boolean persist = false;
    private Hashtable<String,ActiveGame> agHash = new Hashtable<String,ActiveGame>();
    private Vector<ActiveGame> agList = new Vector<ActiveGame>(); 
    private Vector<String> viewerQueue = new Vector<String>();
    private Hashtable<String,String> userList = new Hashtable<String,String>();
    private String hiScoreHolder = "nobody";
    private int hiScoreValue = -1;
    private GolgiTransportOptions stdGto;
    private GolgiTransportOptions hourGto;
    private GolgiTransportOptions dayGto;
    
    newHiScore.ResultReceiver newHiScoreResultReceiver = new newHiScore.ResultReceiver() {
        @Override
        public void success() {
            System.out.println("Sending new Hi Score: success");
        }
        
        @Override
        public void failure(GolgiException ex) {
            System.out.println("Sending new Hi Score: fail: '" + ex.getErrText() + "'");
        }
    };
    newPB.ResultReceiver newPBResultReceiver = new newPB.ResultReceiver() {
        @Override
        public void success() {
            System.out.println("Sending new PB: success");
        }
        
        @Override
        public void failure(GolgiException ex) {
            System.out.println("Sending new PB: fail: '" + ex.getErrText() + "'");
        }
    };
    
    private void maybeNewHiScore(PlayerInfo playerInfo, int score){
        boolean isNew = false;
        boolean consider = false;
        String who = playerInfo.getName();
        
        if(playerInfo.getOs().compareTo("android") == 0 && playerInfo.getAppVer() >= 6){
            consider = true;
        }
        else if(playerInfo.getOs().compareTo("ios") == 0){
            consider = true;
        }

        if(consider){
            if(score > hiScoreValue){
                hiScoreValue = score;
                hiScoreHolder = who;
                System.out.println("New Hi Score for '" + who + "' with: " + score + " tell " + userList.size() + " users");
                isNew = true;
            }
            HiScoreData hiScoreData = new HiScoreData();
            hiScoreData.setName(hiScoreHolder);
            hiScoreData.setScore(hiScoreValue);
        
            if(isNew){
                saveHiScore();
                Vector<String> v = new Vector<String>();
                synchronized(userList){
                    for(Enumeration<String> e = userList.keys(); e.hasMoreElements();){
                        v.add(e.nextElement());
                    }
                }
                while((v.size() > 0)){
                    String user = v.remove(0);
                    System.out.println("Send to '" + user + "'");
                    newHiScore.sendTo(
                            newHiScoreResultReceiver,
                            dayGto,
                            user,
                            hiScoreData);
                }
            }
        }
    }
    
    private void loadUsers(){
        try{
            BufferedReader br = new BufferedReader(new FileReader("GolgiUsers.txt"));
            userList = new Hashtable<String,String>();
            
            String id;
            while((id = br.readLine()) != null){
                userList.put(id, "");
            }
            System.out.println("Loaded " + userList.size() + " users");
        }
        catch(IOException iex){
        }
    }
    
    private void saveUsers(){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter("GolgiUsers.txt"));
            Vector<String> v = new Vector<String>();
            synchronized(userList){
                for(Enumeration<String> e = userList.keys(); e.hasMoreElements();){
                    v.add(e.nextElement());
                }
            }
            while((v.size() > 0)){
                String user = v.remove(0);
                bw.write(user  + "\n");
            }
            bw.close();
        }
        catch(IOException ioex){
        }
        
    }
    
    private void loadHiScore(){
        try{
            BufferedReader br = new BufferedReader(new FileReader("HiScore.txt"));
            String name = br.readLine();
            String valStr = br.readLine();
            try{
                int val = Integer.valueOf(valStr);
                hiScoreHolder = name;
                hiScoreValue = val;
            }
            catch(NumberFormatException nfe){
                
            }
        }
        catch(IOException iex){
        }
    }
    private void saveHiScore(){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter("HiScore.txt"));
            bw.write(hiScoreHolder + "\n" + hiScoreValue + "\n");
            bw.close();
        }
        catch(IOException ioex){
        }
    }

    
    
    class Viewer{
        String id;
        long lastSeen;
        ActiveGame game;
        
        Viewer(String id, ActiveGame game){
            this.id = id;
            this.game = game;
            lastSeen = System.currentTimeMillis();
        }
    }

    class ActiveGame{
        PlayerInfo playerInfo;
        String id;
        String name;
        int hiScore;
        int score;
        long lastSeen;
        boolean endedCleanly;
        Timer t;
        
        Vector<Viewer> viewers = new Vector<Viewer>();
        
        void sendUpdate(TapData td){
            lastSeen = System.currentTimeMillis();
            synchronized(viewers){
                score = td.getScore();
                long threshold = System.currentTimeMillis() - 10000;
                for(Enumeration<Viewer> e = viewers.elements(); e.hasMoreElements();){
                    final Viewer v = e.nextElement();
                    String dst = v.id;
                    
                    if(v.lastSeen > threshold){
                        sendTap.sendTo(new sendTap.ResultReceiver(){
                            @Override
                            public void success(){
                                // System.out.println("sendTap() to SLAVE success");
                                v.lastSeen = System.currentTimeMillis();
                            }
                        
                            @Override
                            public void failure(GolgiException ex){
                                System.out.println("sendTap() to SLAVE fail: " + ex.getErrText());
                            }
                        },
                        stdGto,
                        dst,
                        td);
                    }
                }
            }
        }
        
        void gameOver(GameOverData gameOverData){
            lastSeen = System.currentTimeMillis();
            endedCleanly = true;
            System.out.println(name + " ended with score: " + score + " (" + hiScore + ")");
            synchronized(viewers){
                long threshold = System.currentTimeMillis() - 10000;
                for(Enumeration<Viewer> e = viewers.elements(); e.hasMoreElements();){
                    final Viewer v = e.nextElement();
                    String dst = v.id;
                    
                    if(v.lastSeen > threshold){
                        // System.out.println("Received sendTap()");
                        gameOver.sendTo(new gameOver.ResultReceiver(){
                            @Override
                            public void success(){
                                // System.out.println("gameOver() to SLAVE success");
                                v.lastSeen = System.currentTimeMillis();
                            }
                            
                            @Override
                            public void failure(GolgiException ex){
                                System.out.println("gameOver() to SLAVE fail: " + ex.getErrText());
                            }
                        },
                        stdGto,
                        dst,
                        gameOverData);
                    }
                }
            }
            synchronized(agHash){
                t.cancel();
                agHash.remove(id);
            }
            synchronized(agList){
                agList.remove(this);
            }
        }
        
        void addViewer(Viewer v){
            synchronized(viewers){
                viewers.add(v);
            }
            startGame.sendTo(new startGame.ResultReceiver(){
                @Override
                public void success(){
                }
                
                @Override
                public void failure(GolgiException ex){
                    System.out.println("startGame() to SLAVE fail: " + ex.getErrText());
                }
            },
            stdGto,
            v.id,
            playerInfo);
        }
        
        ActiveGame(PlayerInfo playerInfo){
            this.playerInfo = playerInfo;
            id = playerInfo.getGameId();
            name = playerInfo.getName();
            hiScore = playerInfo.getHiScore();
            score = 0;
            
            t = new Timer();
            t.schedule(new TimerTask(){

                @Override
                public void run() {
                    long idle = System.currentTimeMillis() - lastSeen;
                    if(idle > 15000){
                        synchronized(agHash){
                            agHash.remove(id);
                        }
                        synchronized(agList){
                            agList.remove(ActiveGame.this);
                        }

                        System.out.println("Purged active game due to idleness");
                        t.cancel();
                    }
                }
                
            }, 5000, 5000);
        }
    }

    @Override
    public void registerSuccess() {
        System.out.println("Registered successfully with Golgi API");
    }


    @Override
    public void registerFailure() {
        System.err.println("Failed to register with Golgi API");
        System.exit(-1);
    }

    static void abort(String err) {
        System.err.println("Error: " + err);
        System.exit(-1);
    }

    private startGame.RequestReceiver inboundStartGame = new startGame.RequestReceiver(){
        public void receiveFrom(startGame.ResultSender resultSender, PlayerInfo playerInfo){
            System.out.println("Game Started: " + playerInfo.getName() + 
                    " [" + playerInfo.getHiScore() + "] " + 
                    (playerInfo.appVerIsSet() ? "YES" : "NO") + " (" + playerInfo.getAppVer() + ")" +
                    " '" + playerInfo.getOs() + "'");
            maybeNewHiScore(playerInfo, playerInfo.getHiScore());
            ActiveGame ag = new ActiveGame(playerInfo);
            synchronized(userList){
                if(!userList.containsKey(playerInfo.getGolgiId())){
                    userList.put(playerInfo.getGolgiId(), "");
                    System.out.println("We now know about " + userList.size() + " users");
                    saveUsers();
                }
            }
            synchronized(agHash){
                agHash.put(ag.id, ag);
            }
            synchronized(agList){
                agList.add(ag);
            }
            synchronized(viewerQueue){
                if(viewerQueue.size() > 0){
                    for(Enumeration<String> e = viewerQueue.elements(); e.hasMoreElements();){
                        Viewer v = new Viewer(e.nextElement(), ag);
                        ag.addViewer(v);
                    }
                    viewerQueue.removeAllElements();
                }
            }
            
            resultSender.success();
        }
    };

        
    private sendTap.RequestReceiver inboundSendTap = new sendTap.RequestReceiver(){
        public void receiveFrom(sendTap.ResultSender resultSender, TapData tapData){
            ActiveGame ag;
            synchronized(agHash){
                ag = agHash.get(tapData.getGameId());
            }
            if(ag != null){
                ag.sendUpdate(tapData);
            }
            resultSender.success();
        }
    };

    private gameOver.RequestReceiver inboundGameOver = new gameOver.RequestReceiver(){
        public void receiveFrom(gameOver.ResultSender resultSender, GameOverData gameOverData){
            ActiveGame ag;
            synchronized(agHash){
                ag = agHash.get(gameOverData.getGameId());
            }
            if(ag != null){
                maybeNewHiScore(ag.playerInfo, gameOverData.getScore());
                ag.gameOver(gameOverData);
            }
            resultSender.success();
        }
    };

    private streamGame.RequestReceiver inboundStreamGame = new streamGame.RequestReceiver(){
        public void receiveFrom(streamGame.ResultSender resultSender, String golgiId){
            System.out.println("Asked to watch game by: '" + golgiId + "'");
            resultSender.success();
            synchronized(viewerQueue){
                synchronized(agList){
                    if(agList.size() != 0){
                        ActiveGame g = agList.lastElement();
                        Viewer v = new Viewer(golgiId, g);
                        g.addViewer(v);
                    }
                    else{
                        viewerQueue.add(golgiId);
                    }
                }
            }
        }
    };

    private getHiScore.RequestReceiver inboundGetHiScore = new getHiScore.RequestReceiver(){
        public void receiveFrom(getHiScore.ResultSender resultSender, int pooky){
            System.out.println("Asked for Hi Score");
            HiScoreData hsd = new HiScoreData();
            hsd.setName(hiScoreHolder);
            hsd.setScore(hiScoreValue);
            resultSender.success(hsd);
        }
    };

    private newPB.RequestReceiver inboundNewPB = new newPB.RequestReceiver(){
        public void receiveFrom(newPB.ResultSender resultSender, HiScoreData hiScoreData){
            System.out.println("New PB for '" + hiScoreData.getName() + "': " + hiScoreData.getScore());
            resultSender.success();

            Vector<String> v = new Vector<String>();
            synchronized(userList){
                for(Enumeration<String> e = userList.keys(); e.hasMoreElements();){
                    v.add(e.nextElement());
                }
            }
            while((v.size() > 0)){
                String user = v.remove(0);
                System.out.println("Send to '" + user + "'");
                newPB.sendTo(
                             newPBResultReceiver,
                             hourGto,
                             user,
                             hiScoreData);
            }
        }
    };

    private int hkTmp = -1;

    private void houseKeep(){
        int tmp;
        synchronized(agHash){
            tmp = agHash.size() + agList.size();
            if(tmp != hkTmp){
                hkTmp = tmp;
                System.out.println("There are " + agHash.size() + "/" + agList.size() + " active games");
            }
        }
    }
    
    private void looper(){
        Class<GolgiAPI> apiRef = GolgiAPI.class;
        GolgiAPINetworkImpl impl = new GolgiAPINetworkImpl();
        GolgiAPI.setAPIImpl(impl);
        stdGto = new GolgiTransportOptions();
        stdGto.setValidityPeriod(60);

        hourGto = new GolgiTransportOptions();
        hourGto.setValidityPeriod(3600);

        dayGto = new GolgiTransportOptions();
        dayGto.setValidityPeriod(86400);

        loadHiScore();
        loadUsers();
        
        streamGame.registerReceiver(inboundStreamGame);
        startGame.registerReceiver(inboundStartGame);
        sendTap.registerReceiver(inboundSendTap);
        gameOver.registerReceiver(inboundGameOver);
        getHiScore.registerReceiver(inboundGetHiScore);
        newPB.registerReceiver(inboundNewPB);


        // WhozinService.registerDevice.registerReceiver(registerDeviceSS);
        GolgiAPI.register(devKey,
                          appKey,
                          "SERVER",
                          this);
        
        Timer hkTimer;
        hkTimer = new Timer();
        hkTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                houseKeep();
            }
        }, 1000, 1000);
        
        while(true){
            NTL.doSelect();
        }
    }
    
    private Server(String[] args){
        for(int i = 0; i < args.length; i++){
	    if(args[i].compareTo("-devKey") == 0){
		devKey = args[i+1];
		i++;
	    }
	    else if(args[i].compareTo("-appKey") == 0){
		appKey = args[i+1];
		i++;
	    }
	    else{
		System.err.println("Zoikes, unrecognised option '" + args[i] + "'");
		System.exit(-1);;
	    }
        }
	if(devKey == null){
	    System.out.println("No -devKey specified");
	    System.exit(-1);
	}
	else if(appKey == null){
	    System.out.println("No -appKey specified");
	    System.exit(-1);
	}
    }
        
    public static void main(String[] args) {
        (new Server(args)).looper();
    }
}
