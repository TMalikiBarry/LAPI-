package sn.intouch.gu.lonaciapi.ws.utils;

public class NotificationUtils {
    public static String getTypeFromServiceCode(String codeService) {
        if(codeService.contains("PAIEMENT"))
            return "DEPOT_MOMO";
        return "RETRAIT";
    }
}
