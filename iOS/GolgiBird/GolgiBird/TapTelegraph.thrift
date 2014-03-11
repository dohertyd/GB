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

namespace java io.golgi.example.gen

struct PlayerInfo{
    1: required string golgiId,
    2: required string name,
    3: required i32 hiScore,
    4: required string gameId,
    5: required i32 gameSeed,
    6: optional i32 appVer,
    7: optional string os
}

struct TapData{
    1: required string gameId,
    2: required i32 screenOffset,
    3: required i32 playerY,
    4: required i32 deltaY,
    5: required i32 index,
    6: required i32 score
}

struct GameOverData{
    1: required string gameId,
    2: required i32 screenOffset,
    3: required i32 playerY,
    4: required i32 score
}

struct HiScoreData{
    1: required string name,
    2: required i32 score
}


service  TapTelegraph{
    void startGame(1:PlayerInfo playerInfo),
    void sendTap(1:TapData tapData),
    void gameOver(1:GameOverData gameOverData),
    void streamGame(1:string golgiId),
    HiScoreData getHiScore(1:i32 pooky),
    void newHiScore(1:HiScoreData hiScoreData),
    void newPB(1:HiScoreData hiScoreData),
}
