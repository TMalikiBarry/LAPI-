package sn.intouch.gu.lonaciapi.ejb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.CategoryType;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTypeDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String code;
	private String label;

	public CategoryType fromDTO() {
		return CategoryType.builder()
				.code(code)
				.label(label)
				.build();
	}
}
