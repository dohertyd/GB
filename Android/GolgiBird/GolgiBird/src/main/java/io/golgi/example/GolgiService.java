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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import com.openmindnetworks.golgi.api.GolgiAPI;
import com.openmindnetworks.golgi.api.GolgiAPIHandler;

import java.util.ArrayList;

import io.golgi.apiimpl.android.GolgiAbstractService;
import io.golgi.apiimpl.android.GolgiAndroidTransport;
import io.golgi.example.gen.GolgiKeys;
import io.golgi.example.gen.TapData;
import io.golgi.example.gen.TapTelegraphService;

/**
 * Created by briankelly on 13/02/2014.
 */






public class GolgiService extends GolgiAbstractService
{
    private static GolgiService theInstance = null;
    public GolgiAndroidTransport transport;
    private static boolean persistentConn = false;
    private static Object syncObj = new Object();

    public static GolgiService getServiceInstance(){
        return theInstance;
    }


    public static void usePersistentConnection(){
        persistentConn = true;
        GolgiService svc = getServiceInstance();
        if(svc != null && svc.transport != null){
            svc.transport.usePersistentConnection();
        }
    }

    public static void useEphemeralConnection(){
        persistentConn = false;
        GolgiService svc = getServiceInstance();
        if(svc != null && svc.transport != null){
            svc.transport.useEphemeralConnection();
        }
    }

    private static void DBG(String str){
        DBG.write("SVC", str);
    }

    @Override
    public void readyForRegister()
    {
        synchronized(syncObj)
        {
            if(theInstance != null && theInstance != this){
                DBG("**************************");
                DBG("Changing Service Instance");
                DBG("**************************");
            }
            theInstance = this;
        }

        String id = PlayfieldActivity.getGolgiId(this);

        GolgiAPI api = new GolgiAPI(id);

        // Need to initiialize the thrift defined services
        TapTelegraphService.init();

        PlayfieldActivity.regisiterRequestReceivers();


        GolgiAndroidTransport transport = new GolgiAndroidTransport(this, GolgiKeys.DEV_KEY, GolgiKeys.APP_KEY);

        api.setSBI(transport.getSBI());
        transport.setNBI(api.getNBI());
        if (persistentConn) {
            transport.usePersistentConnection();
        } else {
            transport.useEphemeralConnection();
        }

        setAndroidTransport(transport);
        transport.start();

//        registerGolgi(
//                new GolgiAPIHandler() {
//                    @Override
//                    public void registerSuccess() {
//                        DBG("Golgi registration Success");
//                    }
//
//                    @Override
//                    public void registerFailure() {
//                        DBG("Golgi registration Failure");
//                    }
//                },
//                GolgiKeys.DEV_KEY,
//                GolgiKeys.APP_KEY,
//                id);
    }
}

