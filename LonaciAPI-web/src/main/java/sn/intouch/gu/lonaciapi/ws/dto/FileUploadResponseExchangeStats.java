package sn.intouch.gu.lonaciapi.ws.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponseExchangeStats {

    private List<FileUploadResponse> success ;
    private  List<FileUploadResponse> echecs ;
}
