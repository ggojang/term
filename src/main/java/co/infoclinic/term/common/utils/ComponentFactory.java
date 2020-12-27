package co.infoclinic.term.common.utils;

import co.infoclinic.term.snomedct.model.entity.Component;
import co.infoclinic.term.snomedct.model.entity.Concept;
import co.infoclinic.term.snomedct.model.entity.Description;
import co.infoclinic.term.snomedct.model.entity.StatedRelationship;

public class ComponentFactory {
  
	@SuppressWarnings("unchecked")
	public static <T extends Component> T create(SNOMEDCTComponentTypeEnum componentType) throws Exception {
		T component;

		switch (componentType) {
			case CONCEPT:
				component = (T) new Concept();
				break;
			case DESCRIPTION:
				component = (T) new Description();
				break;
			case RELATIONSHIP:
				component = (T) new StatedRelationship();
				break;
			default:
				throw new Exception("Error Create Component : " + componentType.toString());
		}
		
		return component;
	}
}
