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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by briankelly on 13/02/2014.
 */
public class Renderer implements CanvasViewUser{
    private Paint paint;
    private Controller ctrlr;
    private int firstHurdle = 0;
    private boolean gameOver = false;
    private static void DBG(String str){
        DBG.write(str);
    }

    public void newGame(){
        firstHurdle = 0;
        gameOver = false;
    }

    private int[] rawSamples = {
            193, 111,  // Dimensions of the image
            88,0,      // Point samples
            142,5,
            192,50,
            139, 81,
            88,108,
            48,95,
            24,71,
            0,38};

    private int[] cSamples = new int[0];
    @Override
    public void onDraw(CanvasView canvasView, Canvas canvas) {
        Rect src, dst, pipeSrc;
        int px1, py1, px2, py2;
        int x, y, w, h, i, j;
        Bitmap player = ctrlr.playerBitmap;

        px1 = x = ctrlr.playerX;
        py1 = y = ctrlr.playerY;
        w = player.getWidth();
        h = player.getHeight();

        px2 = px1 + w;
        py2 = py1 + h;

        // DBG("Draw " + x + "x" + y + " on " + canvas.getWidth() + " x " + canvas.getHeight());

        src = new Rect(0, 0, w, h);
        dst = new Rect(x, y, x+w, y+h);
        paint.setAlpha(255);
        canvas.drawBitmap(player, src, dst, paint);

        src = new Rect(0, 0, ctrlr.pipeWidth, ctrlr.pipeTopHeight);
        pipeSrc = new Rect(0, 0, ctrlr.pipeWidth, ctrlr.pipeBitmap.getHeight());
        i = firstHurdle;
        firstHurdle = -1;
        for(; i >= 0 && i < ctrlr.hurdles.length; i++){
            Hurdle hu = ctrlr.hurdles[i];
            x = hu.x - ctrlr.screenX;
            if(firstHurdle < 0 && (x + ctrlr.pipeWidth) >= 0){
                firstHurdle = i;
            }
            if(x > ctrlr.screenWidth){
                break;
            }

            if(!hu.cleared){
                if((px1 + px2) / 2 >= (x + ctrlr.pipeWidth/2)){
                    hu.cleared = true;
                    ctrlr.pipePassed();
                }
            }

            y = hu.y1;
            dst = new Rect(x, y, x + ctrlr.pipeWidth, y + ctrlr.pipeTopHeight);
            canvas.drawBitmap(ctrlr.pipeTopBitmap, src, dst, paint);

            dst = new Rect(x, y + ctrlr.pipeTopHeight, x + ctrlr.pipeWidth, y + ctrlr.pipeHeight);
            canvas.drawBitmap(ctrlr.pipeBitmap, pipeSrc, dst, paint);

            y = hu.y2;
            dst = new Rect(x, y, x + ctrlr.pipeWidth, y + ctrlr.pipeTopHeight);
            canvas.drawBitmap(ctrlr.pipeBotBitmap, src, dst, paint);

            dst = new Rect(x, y - ctrlr.pipeHeight, x + ctrlr.pipeWidth, y);
            canvas.drawBitmap(ctrlr.pipeBitmap, pipeSrc, dst, paint);



            if(!ctrlr.watching && px2 >= x && px1 <= (x + ctrlr.pipeWidth)){
                //
                // Ok, we are in the pipe's zone
                //
                if(py2 > hu.y1 || py1 < (hu.y2 + ctrlr.pipeTopHeight)){
                    //
                    // Ok, crude collision detection has fired
                    //

                    if(cSamples.length == 0){
                        DBG("Zoikes, cSamples length is 0 and we have collisions");
                        if(!gameOver){
                            gameOver = true;
                            ctrlr.gameOver();
                        }
                    }
                    else{
                        x = hu.x - ctrlr.screenX;
                        y = hu.y1;


                        for(j = 0; j < cSamples.length; j += 2){
                            if((cSamples[j] + px1) >= x && (cSamples[j] + px1) <= (x + ctrlr.pipeWidth) && ((cSamples[j+1] + py1) >= y || (cSamples[j+1] + py1) <= (hu.y2 + ctrlr.pipeTopHeight))){
                                DBG("Boom[" + j + "]: (" + (cSamples[j] + px1) + "," + (cSamples[j+1] + py1) + ") " + (hu.x - ctrlr.screenX) + ", " + hu.y1 + ", " + (hu.y2 + ctrlr.pipeTopHeight));
                                break;
                            }
                        }

                        if(j < cSamples.length){
                            if(!gameOver){
                                gameOver = true;
                                ctrlr.gameOver();
                            }
                        }
                    }
                }
            }



        }

    }

    public void setupCollisionDetection(){
        double maxX = rawSamples[0];
        double maxY = rawSamples[1];
        double x, y;
        int j = 0;
        cSamples = new int[rawSamples.length - 2];

        for(int i = 2; i < rawSamples.length; i += 2){
            x = rawSamples[i];
            y = rawSamples[i+1];
            cSamples[j++] = (int)(x/maxX * (double)ctrlr.playerWidth);
            cSamples[j++] = (int)(y/maxY * (double)ctrlr.playerHeight);
        }
        DBG("cSamples is " + cSamples.length + " long");
    }

    public Renderer(Controller controller){
        this.ctrlr = controller;
        paint = new Paint();
        ctrlr.setRenderer(this);
    }

}
