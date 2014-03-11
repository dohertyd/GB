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

/**
 * Created by briankelly on 13/02/2014.
 */
public class Hurdle {
    public int x;
    public int y1;
    public int y2;
    public int y3;
    public boolean cleared;

    public Hurdle(Controller ctrlr, int hurdleX){
        x = hurdleX;
        y1 = ctrlr.PIPE_YMIN + Random.next(ctrlr.PIPE_YMAX - ctrlr.PIPE_YMIN);
        y2 = y1 - (ctrlr.PIPE_GAP_HEIGHT + ctrlr.PIPE_TOP_HEIGHT_REF);
        y3 = y1 - ctrlr.PIPE_GAP_HEIGHT;
        x = (int)(x * ctrlr.xScale);
        y1 = (int)(y1 * ctrlr.yScale);
        y2 = (int)(y2 * ctrlr.yScale);
        y3 = (int)(y3 * ctrlr.yScale);

    }
}
