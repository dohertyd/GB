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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.openmindnetworks.golgi.api.GolgiAPI;

import io.golgi.example.gen.GameOverData;
import io.golgi.example.gen.HiScoreData;
import io.golgi.example.gen.PlayerInfo;
import io.golgi.example.gen.TapData;
import io.golgi.example.gen.TapTelegraphService;
import io.golgi.example.gen.TapTelegraphService.*;

public class PlayfieldActivity extends ActionBarActivity
{
    SharedPreferences sharedPrefs;
    boolean inFg;
    CanvasView playfield;
    Renderer renderer;
    static Controller controller;
    boolean broadcastGames = false;
    String screenName = "Anonymous";
    static PlayfieldActivity theInstance;

    private static void DBG(String str){
        DBG.write(str);
    }

    public static void startService(Context context){
        DBG("Starting Golgi Service");

        Intent serviceIntent = new Intent();
        serviceIntent.setClassName("io.golgi.example", "io.golgi.example.GolgiService");
        context.startService(serviceIntent);
        DBG("Done");
    }

    private static SharedPreferences getSharedPrefs(Context context){
        return context.getSharedPreferences("GolgiBird", Context.MODE_PRIVATE);
    }

    public static int getHiScore(Context context){
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        if(sharedPrefs.getInt("HI-SCORE-HACK1", 0) == 0){
            sharedPrefs.edit().putInt("HI-SCORE-HACK1", 1).commit();
            sharedPrefs.edit().putInt("HI-SCORE", 0).commit();
        }
        return sharedPrefs.getInt("HI-SCORE", 0);

    }

    public static void setHiScore(Context context, int hiScore){
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        sharedPrefs.edit().putInt("HI-SCORE", hiScore).commit();
    }

    public static String getGolgiId(Context context){
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        String golgiId =  sharedPrefs.getString("GOLGI-ID", "");

        if(golgiId.length() == 0){
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < 20; i++){
                sb.append((char)('A' + (int)(Math.random() * ('z' - 'A'))));
            }

            golgiId = sb.toString();
            sharedPrefs.edit().putString("GOLGI-ID", golgiId).commit();
        }
        DBG("GolgiID: '" + golgiId + "'");

        return golgiId;
    }

    private void newHiScore(HiScoreData hiScoreData, boolean isPb){
        final String type = (isPb ? "Personal-Best" : "Hi-Score");
        if(!inFg){
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(PlayfieldActivity.this);
            mBuilder.setSmallIcon(R.drawable.icon_36);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(PlayfieldActivity.this.getResources(), R.drawable.icon_64));
            mBuilder.setContentTitle("Golgi Bird New " + type);
            mBuilder.setContentText(hiScoreData.getName() + ": " + hiScoreData.getScore());
            mBuilder.setTicker("Golgi Bird: " + hiScoreData.getName() + " new " + type + ": " + hiScoreData.getScore());

            mBuilder.setVibrate(new long[]{500,500});

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);

            Intent resultIntent = new Intent(PlayfieldActivity.this, PlayfieldActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(PlayfieldActivity.this);
            stackBuilder.addParentStack(PlayfieldActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(100);
            mNotificationManager.notify(100, mBuilder.build());

        }
        else{
            final HiScoreData _hiScoreData = hiScoreData;
            playfield.post(new Runnable(){
                public void run(){
                    Toast.makeText(PlayfieldActivity.this, _hiScoreData.getName() + " new " + type + ": " + _hiScoreData.getScore(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    protected static void regisiterRequestReceivers()
    {
        sendTap.registerReceiver(new sendTap.RequestReceiver() {
            @Override
            public void receiveFrom(sendTap.ResultSender resultSender, TapData tapData) {
                DBG("Received a tapData for game: " + tapData.getGameId() + " " + tapData.getIndex() + "(" + tapData.getPlayerY() + ") (" + tapData.getScreenOffset() + ")");
                controller.remoteControl(tapData);
                resultSender.success();
            }
        });

        gameOver.registerReceiver(new gameOver.RequestReceiver() {
            @Override
            public void receiveFrom(gameOver.ResultSender resultSender, GameOverData gameOverData) {
                DBG("Game Over Received: " + gameOverData.getGameId());
                controller.remoteControl(gameOverData);
                resultSender.success();
            }
        });

        startGame.registerReceiver(new startGame.RequestReceiver() {
            @Override
            public void receiveFrom(startGame.ResultSender resultSender, PlayerInfo playerInfo) {
                controller.setRemoteName(playerInfo.getName());
                resultSender.success();
            }
        });

        newHiScore.registerReceiver(new newHiScore.RequestReceiver() {
                                        @Override
                                        public void receiveFrom(newHiScore.ResultSender resultSender, HiScoreData hiScoreData) {
                                            DBG("New hi score for: '" + hiScoreData.getName() + "' with: " + hiScoreData.getScore());
                                            theInstance.newHiScore(hiScoreData, false);
                                            resultSender.success();
                                        }
                                    }
        );

        newPB.registerReceiver(new newPB.RequestReceiver() {
            @Override
            public void receiveFrom(newPB.ResultSender resultSender, HiScoreData hiScoreData) {
                DBG("New PB for: '" + hiScoreData.getName() + "' with: " + hiScoreData.getScore());
                theInstance.newHiScore(hiScoreData, true);
                resultSender.success();
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBG("onCreate()");
        theInstance = this;

        sharedPrefs = this.getSharedPreferences("GolgiBird", Context.MODE_PRIVATE);


        startService(this);

//        sendTap.registerReceiver(new sendTap.RequestReceiver() {
//            @Override
//            public void receiveFrom(sendTap.ResultSender resultSender, TapData tapData) {
//                DBG("Received a tapData for game: " + tapData.getGameId() + " " + tapData.getIndex() + "(" + tapData.getPlayerY() + ") (" + tapData.getScreenOffset() + ")");
//                controller.remoteControl(tapData);
//                resultSender.success();
//            }
//        });
//
//        gameOver.registerReceiver(new gameOver.RequestReceiver() {
//            @Override
//            public void receiveFrom(gameOver.ResultSender resultSender, GameOverData gameOverData) {
//                DBG("Game Over Received: " + gameOverData.getGameId());
//                controller.remoteControl(gameOverData);
//                resultSender.success();
//            }
//        });
//
//        startGame.registerReceiver(new startGame.RequestReceiver() {
//            @Override
//            public void receiveFrom(startGame.ResultSender resultSender, PlayerInfo playerInfo) {
//                controller.setRemoteName(playerInfo.getName());
//                resultSender.success();
//            }
//        });
//
//        newHiScore.registerReceiver(new newHiScore.RequestReceiver() {
//            @Override
//            public void receiveFrom(newHiScore.ResultSender resultSender, HiScoreData hiScoreData) {
//                DBG("New hi score for: '" + hiScoreData.getName() + "' with: " + hiScoreData.getScore());
//                newHiScore(hiScoreData, false);
//                resultSender.success();
//            }
//        }
//        );
//
//        newPB.registerReceiver(new newPB.RequestReceiver() {
//            @Override
//            public void receiveFrom(newPB.ResultSender resultSender, HiScoreData hiScoreData) {
//                DBG("New PB for: '" + hiScoreData.getName() + "' with: " + hiScoreData.getScore());
//                newHiScore(hiScoreData, true);
//                resultSender.success();
//            }
//        });


        setContentView(R.layout.activity_playfield);
        controller = new Controller(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playfield, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PrefsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_playfield, container, false);
            return rootView;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        DBG("onResume()");
        inFg = true;

        {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(100);

        }

        //GolgiAPI.usePersistentConnection();
        GolgiService.usePersistentConnection();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        broadcastGames = settings.getBoolean("BCAST", true);
        screenName = settings.getString("NAME", "Anonymous");
        DBG("Name: " + screenName);
        DBG("Broadcast set to: " + broadcastGames);
        renderer = new Renderer(controller);
        playfield = (CanvasView)this.findViewById(R.id.playfield);
        playfield.setUser(renderer);
        playfield.setOnTouchListener(controller);
        controller.onResume();


    }

    @Override
    public void onPause(){
        super.onPause();
        DBG("onPause()");
        inFg = false;

        //GolgiAPI.useEphemeralConnection();
        GolgiService.useEphemeralConnection();
        playfield.clearUser(renderer);
        playfield.setOnTouchListener(null);
        controller.onPause();
        renderer = null;
        playfield = null;

    }

}
