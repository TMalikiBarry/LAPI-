package sn.intouch.gu.lonaciapi.ejb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CodeServiceMOMO;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveAllResponse {
    private List<CodeServiceMOMO> savedEntries;
    private List<String> duplicateMessages;
    private List<String> alreadySavedMessages;
}
