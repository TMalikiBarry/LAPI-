package sn.intouch.gu.lonaciapi.ws.services.exposed;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.services.LonaciTrxTempService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.TypeTrxService;
import sn.intouch.gu.lonaciapi.ejb.operator.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.operator.services.OperatorService;
import sn.intouch.gu.lonaciapi.ejb.utils.TokenGenerator;
import sn.intouch.gu.lonaciapi.ws.models.NotificationExchange;
import sn.intouch.gu.lonaciapi.ws.models.TransactionNotifResponse;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
public class SendNotificationRestService {
    private final Gson gson = new Gson();
    private final OperatorService operatorService = (OperatorService) JNDIUtils
            .lookUpEJB(EJBRegistry.OperatorServiceBean);
    private final LonaciTrxTempService lonaciTrxTempService = (LonaciTrxTempService) JNDIUtils
            .lookUpEJB(EJBRegistry.LonaciTrxTempServiceBean);
    private final TypeTrxService typeTrxService = (TypeTrxService) JNDIUtils.lookUpEJB(EJBRegistry.TypeTrxServiceBean);

    @RequestMapping(value = "/api/v1/notification", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public TransactionNotifResponse sendNotification(@RequestBody NotificationExchange exchange) {
        TransactionNotifResponse response = new TransactionNotifResponse();
        try {
            if (exchange != null) {
                if (this.areFieldsOk(exchange)) {
                    Operator Operator = operatorService.findByOperatorID(exchange.getOperatorID());
                    if (Operator != null) {
                        TypeTrx typeTrx = typeTrxService.getTypeTrxByCode(exchange.getType());
                        if (typeTrx != null) {
                            String token = TokenGenerator.generateToken();
                            LonaciTrxTemp notif = exchange.buildTransactionTemp();
                            notif.setOperateurLibelle(Operator.getOperatorLabel());
                            notif.setTypeTransaction(exchange.getType());
                            notif.setLonaciTransactionID(token);
                            notif.setCodeService("NO_SERVICE_CODE");
                            notif = lonaciTrxTempService.saveTransaction(notif);
                            if (notif == null) {
                                Date date = new Date(exchange.getDate());
                                notif = lonaciTrxTempService.getTrxTempByIdFromPartnerBetweenDates(
                                        exchange.getOperatorTransactionID(), atStartOfDay(date), atEndOfDay(date));
                                System.out.println("Duplicated Notification :: " + notif);
                                if (notif != null)
                                    token = notif.getLonaciTransactionID();
                            } else
                                System.out.println("New Notification :: " + notif.getIdFromPartner());
                            response.setLonaciTransactionID(token);
                            response.setErrorCode("200");
                            response.setErrorMessage("SUCCESS");
                            return response;
                        } else {
                            response.setErrorCode("404");
                            response.setErrorMessage("Transaction type not found");
                        }
                    } else {
                        response.setErrorCode("404");
                        response.setErrorMessage("Cannot find operator");
                    }
                } else {
                    response.setErrorCode("400");
                    response.setErrorMessage("Please provide the required fields");
                }

            } else {
                response.setErrorCode("500");
                response.setErrorMessage("An error occurred while handling the request");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred while handling the request");
            response.setErrorCode("500");
            response.setErrorMessage("An error occurred while handling the request");
        }
        return response;
    }

    private boolean areFieldsOk(NotificationExchange exchange) {

        return exchange.getOperatorID() != null && !exchange.getOperatorID().isEmpty() && exchange.getType() != null
                && !exchange.getType().isEmpty() && exchange.getOperatorTransactionID() != null
                && !exchange.getOperatorTransactionID().isEmpty() && exchange.getAmount() != null
                && exchange.getDate() != null;
    }

    public static Date atStartOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    public static Date atEndOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    private static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
