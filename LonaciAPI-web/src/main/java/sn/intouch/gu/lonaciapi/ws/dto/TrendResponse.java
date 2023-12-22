package sn.intouch.gu.lonaciapi.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendResponse implements Serializable {
    private List<Map<String, String>> day;
    private List<Map<String, String>> week;
    private List<Map<String, String>> month;
}
