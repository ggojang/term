package co.infoclinic.term.snomedct.model.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 최근 레퍼런스세트 멤버 테이블의 다중 Primary Key 대응 ID 클래스
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestRefsetMemberId implements Serializable {
	private static final long serialVersionUID = 1L;

	private String uuid;
}
