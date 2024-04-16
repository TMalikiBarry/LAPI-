package sn.intouch.gu.lonaciapi.ws.services.exposed;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.LonaciTrxTemp;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Operator;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;
import sn.intouch.gu.lonaciapi.ejb.notification.services.LonaciTrxTempService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.OperatorService;
import sn.intouch.gu.lonaciapi.ejb.notification.services.TypeTrxService;
import sn.intouch.gu.lonaciapi.ejb.utils.TokenGenerator;
import sn.intouch.gu.lonaciapi.ws.models.NotificationExchange;
import sn.intouch.gu.lonaciapi.ws.models.TransactionNotifResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
public class UploadFileRestService {
    private final Gson gson = new Gson();
    private final OperatorService operatorService = (OperatorService) JNDIUtils
            .lookUpEJB(EJBRegistry.OperatorServiceBean);
    private final LonaciTrxTempService lonaciTrxTempService = (LonaciTrxTempService) JNDIUtils
            .lookUpEJB(EJBRegistry.LonaciTrxTempServiceBean);
    private final TypeTrxService typeTrxService = (TypeTrxService) JNDIUtils.lookUpEJB(EJBRegistry.TypeTrxServiceBean);

    //@RequestMapping(value = "/api/v1/upload", method = RequestMethod.POST)
    @PostMapping("/api/v1/upload")
    public TransactionNotifResponse uploadFile(@RequestParam("file") MultipartFile file ) {
        TransactionNotifResponse response = new TransactionNotifResponse();
        // exchange
        NotificationExchange exchange = new NotificationExchange() ;

        System.out.println(file.getOriginalFilename());

        try {

            // Convert MultipartFile to a BufferedReader to read the JSON content
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {

                ParseJson exchangeParse = gson.fromJson(line, ParseJson.class) ;
                exchange.setOperatorID(exchangeParse.getOperatorID());
                exchange.setOperatorTransactionID(exchangeParse.getOperatorTrasactionID());
                exchange.setType(exchangeParse.getType());
                exchange.setAmount(Double.parseDouble(exchangeParse.getAmount()));
                exchange.setDate(Long.parseLong(exchangeParse.getDate()));
                exchange.setUserID(exchangeParse.getUserID());


                    if (exchange != null) {
                        if (this.areFieldsOk(exchange)) {
                            Operator Operator = operatorService.findByOperatorID(exchange.getOperatorID());
                            if (Operator != null) {
                                TypeTrx typeTrx = typeTrxService.getByCode(exchange.getType());
                                if (typeTrx != null) {
                                    String token = TokenGenerator.generateToken();
                                    LonaciTrxTemp notif = exchange.buildTransactionTemp();
                                    notif.setOperateurLibelle(Operator.getOperatorLabel());
                                    notif.setTypeTransaction(exchange.getType());
                                    notif.setLonaciTransactionID(token);
                                    notif.setCodeService("NO_SERVICE_CODE");
                                    notif = lonaciTrxTempService.saveTransaction(notif);

                                    if (notif == null) {
                                        System.out.println("notif null");
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
                                    //return response;
                                }
                                else {
                                    response.setErrorCode("404");
                                    response.setErrorMessage("Transaction type not found");
                                    return response ;
                                }
                            }
                            else {
                                response.setErrorCode("404");
                                response.setErrorMessage("Cannot find operator");
                                return  response ;
                            }
                        }
                        else {
                            response.setErrorCode("400");
                            response.setErrorMessage("Please provide the required fields");
                            return response ;
                        }

                    } else {
                        response.setErrorCode("500");
                        response.setErrorMessage("An error occurred while handling the request");
                        return response ;
                    }

            }

        } catch (IOException e) {
            e.printStackTrace();
            response.setErrorCode("500");
            response.setErrorMessage("An error occurred while reading the file.");

        } catch (Exception e) {
            e.printStackTrace();
            response.setErrorCode("500");
            response.setErrorMessage("An error occurred while processing the file.");

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

    @Getter
    @Setter
    public static class ParseJson {
        private String operatorID;

        private String operatorTrasactionID ;

        private String type ;
        private String amount;
        private String userID;

        private String date;


    }
}

