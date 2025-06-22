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
import sn.intouch.gu.lonaciapi.ws.dto.FileUploadResponse;
import sn.intouch.gu.lonaciapi.ws.dto.FileUploadResponseExchangeStats;
import sn.intouch.gu.lonaciapi.ws.models.NotificationExchange;
import sn.intouch.gu.lonaciapi.ws.models.TransactionNotifResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public Object uploadFile(@RequestParam("file") MultipartFile file ) {

        FileUploadResponseExchangeStats stats  = new FileUploadResponseExchangeStats() ;

        // exchange


        List<FileUploadResponse> success = new ArrayList<>() ;
        List<FileUploadResponse> echecs = new ArrayList<>() ;

        System.out.println(file.getOriginalFilename());

        try {

            // Convert MultipartFile to a BufferedReader to read the JSON content
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {

                NotificationExchange exchange = new NotificationExchange() ;

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

                                        Date date = new Date(exchange.getDate());
                                        notif = lonaciTrxTempService.getTrxTempByIdFromPartnerAndTypeBetweenDates(
                                                exchange.getOperatorTransactionID(), exchange.getType(), atStartOfDay(date), atEndOfDay(date));
                                        System.out.println("Duplicated Notification :: " + notif);
                                        if (notif != null)
                                            token = notif.getLonaciTransactionID();
                                    } else
                                        System.out.println("New Notification :: " + notif.getIdFromPartner());

                                    TransactionNotifResponse response = new TransactionNotifResponse();
                                    response.setLonaciTransactionID(token);
                                    response.setErrorCode("200");
                                    response.setErrorMessage("SUCCESS");
                                    success.add(new FileUploadResponse(response, exchange)) ;
                                    //return response;
                                }
                                else {
                                    TransactionNotifResponse response = new TransactionNotifResponse();
                                    response.setErrorCode("404");
                                    response.setErrorMessage("Transaction type not found");
                                    echecs.add(new FileUploadResponse(response, exchange)) ;
                                }
                            }
                            else {
                                TransactionNotifResponse response = new TransactionNotifResponse();
                                response.setErrorCode("404");
                                response.setErrorMessage("Cannot find operator");
                                echecs.add(new FileUploadResponse(response, exchange)) ;

                            }
                        }
                        else {
                            TransactionNotifResponse response = new TransactionNotifResponse();
                            response.setErrorCode("400");
                            response.setErrorMessage("Please provide the required fields");
                            echecs.add(new FileUploadResponse(response, exchange)) ;
                        }

                    } else {
                        TransactionNotifResponse response = new TransactionNotifResponse();
                        response.setErrorCode("500");
                        response.setErrorMessage("An error occurred while handling the request");
                        return response ;
                    }

            }


            stats.setEchecs(echecs);
            stats.setSuccess(success);
            return stats ;

        } catch (IOException e) {

            TransactionNotifResponse response = new TransactionNotifResponse();
            e.printStackTrace();
            response.setErrorCode("500");
            response.setErrorMessage("An error occurred while reading the file.");
            return response ;

        } catch (Exception e) {

            TransactionNotifResponse response = new TransactionNotifResponse();
            e.printStackTrace();
            response.setErrorCode("500");
            response.setErrorMessage("An error occurred while processing the file.");
            return response ;

        }


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

