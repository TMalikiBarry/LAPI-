package sn.intouch.gu.lonaciapi.ejb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeDirection;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;

import javax.persistence.Column;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeTrxDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String code;
	private String label;

	private TypeDirection direction;

	private String category;

	private Boolean useToCompute = false;

	public TypeTrx fromDTO() {
		return TypeTrx.builder()
				.code(code)
				.label(label)
				.category(category)
				.useToCompute(useToCompute)
				.direction(direction)
				.build();
	}
}
