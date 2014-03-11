//
// This Software (the “Software”) is supplied to you by Openmind Networks Limited ("Openmind")
// your use, installation, modification or redistribution of this Software constitutes acceptance
// of this disclaimer.
// If you do not agree with the terms of this disclaimer, please do not use, install, modify
// or redistribute this Software.
//
// TO THE MAXIMUM EXTENT PERMITTED BY LAW, THE SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION,
// ANY WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE.
//
// Each user of the Software is solely responsible for determining the appropriateness of
// using and distributing the Software and assumes all risks associated with use of the Software,
// including but not limited to the risks and costs of Software errors, compliance with
// applicable laws, damage to or loss of data, programs or equipment, and unavailability or
// interruption of operations.
//
// TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW OPENMIND SHALL NOT HAVE ANY LIABILITY FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// WITHOUT LIMITATION, LOST PROFITS, LOSS OF BUSINESS, LOSS OF USE, OR LOSS OF DATA), HOWSOEVER
// CAUSED UNDER ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
//


package io.golgi.example;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.openmindnetworks.golgi.api.GolgiException;
import com.openmindnetworks.golgi.api.GolgiTransportOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import io.golgi.example.gen.GameOverData;
import io.golgi.example.gen.HiScoreData;
import io.golgi.example.gen.PlayerInfo;
import io.golgi.example.gen.TapData;
import io.golgi.example.gen.TapTelegraphService;
import io.golgi.example.gen.TapTelegraphService.newPB;

/**
 * Created by briankelly on 13/02/2014.
 */
public class Controller implements View.OnTouchListener{
    public static final int SCREEN_WIDTH_REF = 1080;
    public static final int SCREEN_HEIGHT_REF = 1557;
    public static final int PLAYER_WIDTH_REF = 192;
    public static final int PLAYER_HEIGHT_REF = 112;
    public static final int PIPE_GAP_HEIGHT = 350;
    public static final int PIPE_YMIN = PIPE_GAP_HEIGHT + 100;
    public static final int PIPE_YMAX = SCREEN_HEIGHT_REF - PIPE_YMIN;

    public static final int PIPE_WIDTH_REF = 200;
    public static final int PIPE_TOP_HEIGHT_REF = 100;
    public static final int PIPE_SPACING = 700;
    private static final double BOOST = 1000.0;
    private static final double GRAVITY = 4000.0;
    private static final double TERMINALV = 2000.0;
    private static final double MAXBOOST= BOOST * 1.5;

    public int screenWidth;
    public int pipeWidth;
    public int pipeHeight;
    public int pipeTopHeight;
    public int playerWidth;
    public int playerHeight;
    public static double xScale = 0.0;
    public static double yScale = 0.0;

    PlayfieldActivity activity;
    CanvasView playfield;
    Renderer renderer;
    Bitmap playerBitmap;
    Bitmap pipeBitmap;
    Bitmap pipeTopBitmap;
    Bitmap pipeBotBitmap;
    boolean watching = false;

    long now, lastTime;
    private boolean choosing = false;
    private boolean starting = false;
    private boolean running = false;
    private boolean ended = false;
    private long endOfGame = 0;
    private int gameSeed = 12345;
    private int tapIndex = 1;
    private String gameId = "";

    TextView nameTv;
    TextView scoreTv;
    TextView hiScoreTv;
    TextView gameOverTv;
    TextView tapToStartTv;
    TextView netHiScoreTv;
    Button playButton;
    Button watchButton;




    int playerX;
    int playerY;
    int playerYglobal;

    int screenX = 0;
    int screenXglobal = 0;
    int screenXglobalMax = 0;

    Hurdle[] hurdles = new Hurdle[0];

    int maxY;
    int maxYglobal;
    int remoteScore = 0;
    String remoteGameId = "";
    String remoteName = "";
    int score = 0;
    int hiScore = 0;
    int personalBest = 0;

    double dY = 0;
    double dX = 300.0;

    private boolean resumed;
    private boolean resized = false;
    private Timer refreshTimer;


    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;

        if(resized){
            renderer.setupCollisionDetection();
        }
    }

    private TapData lastTd = null;

    private Handler gameStartingHndlr = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            gameStartingVisuals();
        }
    };

    private Handler gameRunningHndlr = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            gameRunningVisuals();
        }
    };

    private Handler gameOverHndlr = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            gameOverVisuals();
        }
    };


    private GameOverData god = null;
    private Vector<TapData> tdQueue = new Vector<TapData>();


    public void remoteControl(GameOverData god){
        this.god = god;
    }

    private void nextTapData(boolean force){
        TapData td;

        if(force || screenXglobal >= screenXglobalMax){
            if(tdQueue.size() == 0){
                if(god != null){
                    screenXglobal = god.getScreenOffset();
                    playerYglobal = god.getPlayerY();
                    screenXglobalMax = god.getScreenOffset();

                    playerY = (int)(playerYglobal * yScale);
                    screenX = (int)(screenXglobal * xScale);

                    remoteScore = lastTd.getScore();

                    gameOverHndlr.sendEmptyMessage(0);
                    god = null;
                }
            }
            else{
                TapData tapData;

                synchronized (tdQueue){
                    tapData = tdQueue.remove(0);
                }

                String id = tapData.getGameId();
                if(id.compareTo(remoteGameId) != 0){
                    DBG("New Game");
                    remoteGameId = id;

                    generateHurdles(gameSeed);
                    renderer.newGame();
                    lastTd = null;
                    screenXglobal = 0;
                    screenXglobalMax = 0;
                    screenX = 0;
                    gameRunningHndlr.sendEmptyMessage(0);
                }

                if(lastTd == null){
                    lastTd = tapData;
                }
                else if(lastTd.getIndex() != tapData.getIndex() - 1){
                    lastTd = tapData;
                }
                else{
                    //
                    // Ok, we have a pair of TapData instances.
                    // Move the state of the game to the older
                    // of the two and allow the game to automatically
                    // progress to the screen offset of the newer
                    // tapData
                    //

                    screenXglobal = lastTd.getScreenOffset();
                    playerYglobal = lastTd.getPlayerY();
                    dY = (double)(lastTd.getDeltaY()) / 1000.0;

                    screenXglobalMax = tapData.getScreenOffset();

                    playerY = (int)(playerYglobal * yScale);
                    screenX = (int)(screenXglobal * xScale);



                    remoteScore = lastTd.getScore();

                    lastTd = tapData;
                }

            }
        }
    }

    Handler remoteNameHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            nameTv.setText(remoteName);
        }
    };

    public void setRemoteName(String name){
        remoteName = name;
        remoteNameHandler.sendEmptyMessage(0);
    }

    public void remoteControl(TapData tapData){
        synchronized (tdQueue){
            if(tdQueue.size() > 5){
                tdQueue.remove(0);
            }
            tdQueue.add(tapData);
            DBG("TapData Queue is Now: " + tdQueue.size() + screenXglobal + "(" + screenXglobalMax + ")");
        }
        if(tapData.getGameId().compareTo(remoteGameId) != 0){
            nextTapData(true);
        }
    }

    public void modeChoiceVisuals(){
        choosing = true;
        starting = false;
        running = false;
        ended = false;

        screenXglobal = 0;
        screenX = 0;
        playerX = 0;
        playerY = 0;

        showTextViews();

        GolgiTransportOptions gto = new GolgiTransportOptions();
        gto.setValidityPeriod(60);

        TapTelegraphService.getHiScore.sendTo(new TapTelegraphService.getHiScore.ResultReceiver() {
            @Override
            public void failure(GolgiException ex) {
                DBG("Sending getHiScore fail: " + ex.getErrText());
            }

            @Override
            public void success(final HiScoreData hiScoreData) {
                netHiScoreTv.post(new Runnable(){
                    @Override
                    public void run() {
                        netHiScoreTv.setText(hiScoreData.getName() + ": " + hiScoreData.getScore());
                    }
                });
                DBG("Sending GetHiScore success");
            }
        }, gto, "SERVER", 1);

    }


    public void gameStartingVisuals(){
        choosing = false;
        starting = true;
        running = false;
        ended = false;
        score = 0;
        scoreTv.setText("0");
        hiScoreTv.setText("" + PlayfieldActivity.getHiScore(activity));
        showTextViews();
    }


    public void gameRunningVisuals(){
        choosing = false;
        starting = false;
        running = true;
        ended = false;
        showTextViews();
    }

    public void gameOverVisuals(){
        choosing = false;
        starting = false;
        running = false;
        ended = true;
        showTextViews();
        playfield.invalidate();
    }




    public void gameOver(){
        gameOverVisuals();
        endOfGame = System.currentTimeMillis();

        if(!watching){
            if(activity.broadcastGames){
                GolgiTransportOptions gto = new GolgiTransportOptions();
                gto.setValidityPeriod(60);


                GameOverData god = new GameOverData();
                god.setGameId(gameId);
                god.setScreenOffset(screenXglobal);
                god.setPlayerY(playerYglobal);
                god.setScore(score);

                DBG("Sending Game Over");
                TapTelegraphService.gameOver.sendTo( new TapTelegraphService.gameOver.ResultReceiver() {
                    @Override
                    public void failure(GolgiException ex) {
                        DBG("Game Over Send failed: " + ex.getErrText());
                    }

                    @Override
                    public void success() {
                        DBG("Game Over Sent Successfully");
                    }
                }, gto, "SERVER", god);

                if((hiScore > personalBest) && hiScore >= 10){
                    //
                    //
                    //
                    personalBest = hiScore;
                    HiScoreData hsd = new HiScoreData();
                    hsd.setName(activity.screenName);
                    hsd.setScore(personalBest);

                    newPB.sendTo(new newPB.ResultReceiver() {
                        @Override
                        public void failure(GolgiException ex) {
                            DBG("New PB send fail: " + ex.getErrText());
                        }

                        @Override
                        public void success() {
                            DBG("New PB sent successfully");
                        }
                    }, gto, "SERVER", hsd);
                }
            }
        }
    }

    public void pipePassed(){
        if(!watching){
            score++;
            scoreTv.setText("" + score);
            if(score > hiScore){
                hiScore = score;
                PlayfieldActivity.setHiScore(activity, hiScore);
                hiScoreTv.setText("" + hiScore);
            }
        }
    }

    private void newGame(){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < 10; i++){
            sb.append((char)('A' + (int)(Math.random() * ('z' - 'A'))));
        }

        gameId = sb.toString();

        gameStartingVisuals();
        showTextViews();
        dY = 0;
        screenXglobal = 0;
        playerYglobal = (SCREEN_HEIGHT_REF - PLAYER_HEIGHT_REF)/2;


        playerX = (playfield.getWidth() / 3) - (playerBitmap.getWidth() / 2);
        playerY = (int)(playerYglobal * yScale);
        screenX = (int)(screenXglobal * xScale);

        generateHurdles(gameSeed);
        if(renderer != null){
            renderer.newGame();
        }

    }

    private void resizeBitmaps(){
        //
        // Ok, our "native" resolution is going to be 1080 x 1557. This is the size
        // on a Nexus 5. We will scale accordingly on others.
        //
        resized = true;

        screenWidth = playfield.getWidth();

        xScale = (double)playfield.getWidth() / (double)SCREEN_WIDTH_REF;
        yScale = (double)playfield.getHeight() / (double)SCREEN_HEIGHT_REF;

        DBG("Need to resize bitmaps for screen: " + playfield.getWidth() + "x" + playfield.getHeight());

        playerBitmap = Bitmap.createScaledBitmap(
                playerBitmap,
                (int)(xScale * PLAYER_WIDTH_REF),
                (int)(yScale * PLAYER_HEIGHT_REF),
                false);

        playerWidth = playerBitmap.getWidth();
        playerHeight = playerBitmap.getHeight();

        pipeTopBitmap = Bitmap.createScaledBitmap(
                pipeTopBitmap,
                (int)(xScale * PIPE_WIDTH_REF),
                (int)(yScale * PIPE_TOP_HEIGHT_REF),
                false);

        pipeWidth = pipeTopBitmap.getWidth();
        pipeTopHeight = pipeTopBitmap.getHeight();

        Matrix matrix = new Matrix();

        matrix.preScale(1.0f, -1.0f);
        pipeBotBitmap = Bitmap.createBitmap(pipeTopBitmap, 0, 0, pipeTopBitmap.getWidth(), pipeTopBitmap.getHeight(), matrix, true);


        pipeBitmap = Bitmap.createScaledBitmap(
                pipeBitmap,
                (int)(xScale * PIPE_WIDTH_REF),
                2000,
                false);

        pipeHeight = pipeBitmap.getHeight();

    }

    private void move(){
        double fac = (double)(now - lastTime) / 1000.0;

        if(watching){
            nextTapData(false);
        }

        if(!watching || screenXglobal < screenXglobalMax){
            screenXglobal += dX * fac;
            playerYglobal += dY * fac;


            screenX = (int)(screenXglobal * xScale);
            playerY = (int)(playerYglobal * yScale);

            if(playerYglobal > maxYglobal && !watching){
                playerYglobal = maxYglobal;
                playerY = maxY;
                dY = 0.0;
                gameOver();
            }
            else{
                dY += (GRAVITY * fac);
                if(dY > TERMINALV){
                    dY = TERMINALV;
                }
            }
        }
    }

    private void generateHurdles(int seed){
        ArrayList<Hurdle> al = new ArrayList<Hurdle>();
        int i, x;
        Random.seed(seed);
        x = (SCREEN_WIDTH_REF * 3)/2;
        for(i = 0; i < 500; i++){
            al.add(new Hurdle(this, x));
            x += PIPE_SPACING;
        }

        hurdles = al.toArray(new Hurdle[0]);
    }

    private void showTextViews(){
        hiScoreTv.setVisibility(View.INVISIBLE);
        scoreTv.setVisibility(View.INVISIBLE);
        gameOverTv.setVisibility(View.INVISIBLE);
        tapToStartTv.setVisibility(View.INVISIBLE);
        nameTv.setVisibility(View.INVISIBLE);
        netHiScoreTv.setVisibility(View.INVISIBLE);

        if(choosing){
            playButton.setVisibility(View.VISIBLE);
            watchButton.setVisibility(View.VISIBLE);
            netHiScoreTv.setVisibility(View.VISIBLE);
        }
        else{
            playButton.setVisibility(View.INVISIBLE);
            watchButton.setVisibility(View.INVISIBLE);
            hiScoreTv.setVisibility(View.VISIBLE);
            scoreTv.setVisibility(View.VISIBLE);

            if(watching){
                nameTv.setVisibility(View.VISIBLE);
            }

            if(starting){
                if(!watching){
                    tapToStartTv.setVisibility(View.VISIBLE);
                }
            }
            else if(ended){
                gameOverTv.setVisibility(View.VISIBLE);
            }
        }

    }

    private void resumeWork(){
        if(!resized){
            DBG("***************************************************");
            DBG("***************************************************");
            DBG("***************************************************");
            resizeBitmaps();
            renderer.setupCollisionDetection();
            DBG("***************************************************");
            DBG("***************************************************");
            DBG("***************************************************");
        }

        maxYglobal = SCREEN_HEIGHT_REF - PLAYER_HEIGHT_REF;

        maxY = (int)(maxYglobal * yScale);

        playButton = (Button)activity.findViewById(R.id.playButton);
        watchButton = (Button)activity.findViewById(R.id.watchButton);
        nameTv = (TextView)activity.findViewById(R.id.nameTextView);
        scoreTv = (TextView)activity.findViewById(R.id.scoreTextView);
        hiScoreTv = (TextView)activity.findViewById(R.id.hiScoreTextView);
        gameOverTv = (TextView)activity.findViewById(R.id.gameOverTextView);
        tapToStartTv = (TextView)activity.findViewById(R.id.startTextView);
        netHiScoreTv = (TextView)activity.findViewById(R.id.netHiScoreTextView);

        netHiScoreTv.setText("");

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watching = false;
                newGame();

            }
        });

        watchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watching = true;
                nameTv.setText("Waiting...");
                newGame();

                GolgiTransportOptions gto = new GolgiTransportOptions();
                gto.setValidityPeriod(60);


                TapTelegraphService.streamGame.sendTo(new TapTelegraphService.streamGame.ResultReceiver() {
                    @Override
                    public void failure(GolgiException ex) {

                    }

                    @Override
                    public void success() {

                    }
                }, gto, "SERVER", PlayfieldActivity.getGolgiId(activity));
            }
        });

        modeChoiceVisuals();
    }

    private Handler redrawHandler = new Handler(){
        public void handleMessage(Message msg){
            if(playfield != null && playfield.getWidth() > 0){
                now = System.currentTimeMillis();
                if(resumed){

                    resumeWork();

                    lastTime = now;
                    resumed = false;
                }

                if(watching){
                    if(remoteScore != score && scoreTv != null){
                        score = remoteScore;
                        scoreTv.setText("" + score);
                    }
                }

                if(running){
                    move();
                }

                if(!ended){
                    playfield.invalidate();
                }

                lastTime = now;
            }
        }
    };

    private void startRefreshTimer(){
        if(refreshTimer == null){
            DBG("Starting refreshTimer()");
            refreshTimer = new Timer();
            refreshTimer.schedule(new TimerTask(){
                @Override
                public void run() {
                    redrawHandler.sendEmptyMessage(0);
                }
            }, 20, 20);
        }
    }

    private void stopRefreshTimer(){
        if(refreshTimer != null){
            DBG.write("Stopping refreshTimer()");
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private static void DBG(String str){
        DBG.write(str);
    }

    public void onResume(){
        this.resumed = true;
        this.playfield = activity.playfield;

        startRefreshTimer();
    }

    public void onPause(){
        stopRefreshTimer();
        this.resumed = false;
        playfield = null;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // DBG("Capturing Touch: " + event.toString());
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(watching){
                // Ignoring touch in slave mode.
            }
            else{
                if(starting){
                    gameRunningVisuals();

                    if(activity.broadcastGames){
                        PlayerInfo playerInfo = new PlayerInfo();

                        playerInfo.setGameId(gameId);
                        playerInfo.setGolgiId(PlayfieldActivity.getGolgiId(activity));
                        playerInfo.setName(activity.screenName);
                        playerInfo.setHiScore(hiScore);
                        playerInfo.setGameSeed(gameSeed);

                        try {
                            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                            playerInfo.setAppVer(packageInfo.versionCode);
                        }
                        catch (PackageManager.NameNotFoundException e) {
                            playerInfo.setAppVer(-1);
                        }

                        playerInfo.setOs("android");
                        GolgiTransportOptions gto = new GolgiTransportOptions();
                        gto.setValidityPeriod(60);

                        TapTelegraphService.startGame.sendTo(new TapTelegraphService.startGame.ResultReceiver() {
                            @Override
                            public void failure(GolgiException ex) {
                                DBG("startGame() failure: " + ex.getErrText());
                            }

                            @Override
                            public void success() {
                                DBG("startGame() success");
                            }
                        }, gto, "SERVER", playerInfo);

                    }
                }

                if(running){
                    if(dY > 0.0){
                        dY = 0.0;
                    }
                    dY -= BOOST;
                    if(dY < -MAXBOOST){
                        dY = -MAXBOOST;
                    }

                    if(activity.broadcastGames){
                        TapData tapData = new TapData();

                        tapData.setGameId(gameId);
                        tapData.setScreenOffset(screenXglobal);
                        tapData.setPlayerY(playerYglobal);
                        tapData.setDeltaY((int) (dY * 1000));
                        tapData.setIndex(tapIndex++);
                        tapData.setScore(score);
                        GolgiTransportOptions gto = new GolgiTransportOptions();
                        gto.setValidityPeriod(60);

                        DBG("Sending TapData");
                        TapTelegraphService.sendTap.sendTo(new TapTelegraphService.sendTap.ResultReceiver() {
                            @Override
                            public void failure(GolgiException ex) {
                                DBG("Sending TapData fail: " + ex.getErrText());
                            }

                            @Override
                            public void success() {
                                DBG("Sending TapData success");
                            }
                        }, gto, "SERVER", tapData);
                    }
                }
            }

            if(ended){
                long t = System.currentTimeMillis() - endOfGame;
                if(t > 2000){
                    modeChoiceVisuals();
                }
            }
        }

        return true;
    }

    public Controller(PlayfieldActivity activity){
        this.activity = activity;

        BitmapFactory.Options options = new BitmapFactory.Options();

        hiScore = PlayfieldActivity.getHiScore(activity);
        personalBest = hiScore;
        playerBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.player, options);
        pipeBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.pipe, options);
        pipeTopBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.pipe_top, options);
        DBG("Loaded player bitmap: " + playerBitmap.getWidth() + "x" + playerBitmap.getHeight());

    }
}
