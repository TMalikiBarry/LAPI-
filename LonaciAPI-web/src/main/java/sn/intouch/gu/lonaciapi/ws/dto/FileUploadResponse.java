package sn.intouch.gu.lonaciapi.ws.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ws.models.NotificationExchange;
import sn.intouch.gu.lonaciapi.ws.models.TransactionNotifResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private TransactionNotifResponse response ;
    private NotificationExchange exchange ;

}
