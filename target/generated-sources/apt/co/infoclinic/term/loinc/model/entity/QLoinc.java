package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLoinc is a Querydsl query type for Loinc
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLoinc extends EntityPathBase<Loinc> {

    private static final long serialVersionUID = -946537676L;

    public static final QLoinc loinc = new QLoinc("loinc");

    public final StringPath askAtOrderEntry = createString("askAtOrderEntry");

    public final StringPath associatedObservations = createString("associatedObservations");

    public final StringPath cdiscCommonTests = createString("cdiscCommonTests");

    public final StringPath changeReasonPublic = createString("changeReasonPublic");

    public final StringPath changeType = createString("changeType");

    public final StringPath className = createString("className");

    public final NumberPath<Integer> classType = createNumber("classType", Integer.class);

    public final StringPath code = createString("code");

    public final NumberPath<Integer> commonOrderRank = createNumber("commonOrderRank", Integer.class);

    public final NumberPath<Integer> commonSiTestRank = createNumber("commonSiTestRank", Integer.class);

    public final NumberPath<Integer> commonTestRank = createNumber("commonTestRank", Integer.class);

    public final StringPath component = createString("component");

    public final StringPath consumerName = createString("consumerName");

    public final StringPath definitionDescription = createString("definitionDescription");

    public final StringPath displayName = createString("displayName");

    public final StringPath exampleAnswers = createString("exampleAnswers");

    public final StringPath exampleSiUcumUnits = createString("exampleSiUcumUnits");

    public final StringPath exampleUcumUnits = createString("exampleUcumUnits");

    public final StringPath exampleUnits = createString("exampleUnits");

    public final StringPath externalCopyrightLink = createString("externalCopyrightLink");

    public final StringPath externalCopyrightNotice = createString("externalCopyrightNotice");

    public final StringPath firstReleasedVersion = createString("firstReleasedVersion");

    public final StringPath formula = createString("formula");

    public final StringPath hl7AttachmentStructure = createString("hl7AttachmentStructure");

    public final StringPath hl7FieldSubfieldId = createString("hl7FieldSubfieldId");

    public final StringPath lastChangedVersion = createString("lastChangedVersion");

    public final StringPath longCommonName = createString("longCommonName");

    public final StringPath methodType = createString("methodType");

    public final StringPath orderObs = createString("orderObs");

    public final StringPath panelType = createString("panelType");

    public final StringPath property = createString("property");

    public final StringPath relatedNames2 = createString("relatedNames2");

    public final StringPath scaleType = createString("scaleType");

    public final StringPath shortName = createString("shortName");

    public final StringPath status = createString("status");

    public final StringPath statusReason = createString("statusReason");

    public final StringPath statusText = createString("statusText");

    public final StringPath submittedUnits = createString("submittedUnits");

    public final StringPath surveyQuestSrc = createString("surveyQuestSrc");

    public final StringPath surveyQuestText = createString("surveyQuestText");

    public final StringPath system = createString("system");

    public final StringPath timeAspect = createString("timeAspect");

    public final StringPath unitsAndRange = createString("unitsAndRange");

    public final StringPath unitsRequired = createString("unitsRequired");

    public final StringPath validHL7AttachmentRequest = createString("validHL7AttachmentRequest");

    public QLoinc(String variable) {
        super(Loinc.class, forVariable(variable));
    }

    public QLoinc(Path<? extends Loinc> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLoinc(PathMetadata<?> metadata) {
        super(Loinc.class, metadata);
    }

}

