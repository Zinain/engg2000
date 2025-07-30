package CCP.src;

import org.json.JSONObject;

public class Command extends JSONObject {
    static JSONObject shared = new JSONObject();
    String client_type;

    public Command(String inMes, int inSeq, String inAct, String inStat, String inStationID) {
        client_type = "CCP";
        put("client_type", client_type);
        put("message", inMes);
        put("client_id", "BR11");
        put("sequence_number", inSeq);

        if (inAct != null) {
            put("action", inAct);
        }
        if (inStat != null) {
            put("status", inStat);
        }
        if (inStationID != null) {
            put("station_id", inStationID);
        }
    }


    public synchronized void setMessage(JSONObject send){
        shared = send;
        notify();
    }

    public synchronized JSONObject getMessage() throws InterruptedException{
        while(shared == null){
            wait();
        }
        return shared;
    }
}
