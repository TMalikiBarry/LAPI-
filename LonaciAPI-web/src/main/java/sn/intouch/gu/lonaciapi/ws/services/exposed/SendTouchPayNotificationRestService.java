package sn.intouch.gu.lonaciapi.ws.services.exposed;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;
import sn.intouch.gu.lonaciapi.ejb.notification.services.LonaciTrxTempService;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.services.OperatorService;
import sn.intouch.gu.lonaciapi.ws.models.TransactionNotifResponse;
import sn.intouch.gu.lonaciapi.ws.models.USSDOperationExchange;
import sn.intouch.gu.lonaciapi.ws.utils.NotificationUtils;

@RestController
public class SendTouchPayNotificationRestService {
    private final Gson gson = new Gson();
    private final OperatorService operatorService = (OperatorService) JNDIUtils
            .lookUpEJB(EJBRegistry.OperatorServiceBean);
    private final LonaciTrxTempService lonaciTrxTempService = (LonaciTrxTempService) JNDIUtils
            .lookUpEJB(EJBRegistry.LonaciTrxTempServiceBean);


    @RequestMapping(value = "/api/v1/notify-touchpay", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public String sendNotification(@RequestBody USSDOperationExchange exchange) {
        TransactionNotifResponse response = new TransactionNotifResponse();
        try {
            if (exchange != null) {
                if (this.areFieldsOk(exchange)) {
                    Operator operateur = operatorService.findByOperatorID(exchange.getCodeSalePoint());
                    if (operateur != null) {
                        LonaciTrxTemp trx = exchange.buildLonaciTrxTempFromOperation();
                        trx.setOperateurLibelle(operateur.getOperatorLabel());
                        trx.setTypeTransaction(NotificationUtils.getTypeFromServiceCode(exchange.getCodeService()));

                        lonaciTrxTempService.saveTransaction(trx);
                        response.setLonaciTransactionID(exchange.getToken());

                        response.setErrorCode("200");
                        response.setErrorMessage("Success");
                        return gson.toJson(response);
                    } else {
                        response.setErrorCode("404");
                        response.setErrorMessage("Can't find the operateur");
                    }
                } else {
                    response.setErrorCode("400");
                    response.setErrorMessage("Fields cannot be null or empty");
                }

            } else {
                response.setErrorCode("500");
                response.setErrorMessage("Error during deserialization.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setErrorCode("500");
            response.setErrorMessage("An exception occurred while processing the request");
        }
        return gson.toJson(response);
    }

    private boolean areFieldsOk(USSDOperationExchange exchange) {

        return exchange.getCodeSalePoint() != null && !exchange.getCodeSalePoint().equals("")
                && exchange.getPartnerDistTransactionId() != null && !exchange.getPartnerDistTransactionId().equals("")
                && exchange.getCodeService() != null && !exchange.getCodeService().equals("")
                && exchange.getMontant() != null && exchange.getDestinataire() != null
                && !exchange.getDestinataire().equals("")
                && exchange.getToken() != null;
    }
}
