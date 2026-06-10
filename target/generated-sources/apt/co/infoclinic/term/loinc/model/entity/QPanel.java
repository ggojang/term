package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QPanel is a Querydsl query type for Panel
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QPanel extends EntityPathBase<Panel> {

    private static final long serialVersionUID = -943256131L;

    public static final QPanel panel = new QPanel("panel");

    public final StringPath allowableAlternative = createString("allowableAlternative");

    public final StringPath answerCardinality = createString("answerCardinality");

    public final StringPath answerListIdOverride = createString("answerListIdOverride");

    public final StringPath answerListTypeOverride = createString("answerListTypeOverride");

    public final StringPath answerSequenceOverride = createString("answerSequenceOverride");

    public final StringPath code = createString("code");

    public final StringPath codingInstructions = createString("codingInstructions");

    public final StringPath conditionForInclusion = createString("conditionForInclusion");

    public final StringPath consistencyChecks = createString("consistencyChecks");

    public final StringPath context = createString("context");

    public final StringPath dataTypeInForm = createString("dataTypeInForm");

    public final StringPath dataTypeSource = createString("dataTypeSource");

    public final StringPath defaultValue = createString("defaultValue");

    public final StringPath displayNameForForm = createString("displayNameForForm");

    public final StringPath entryType = createString("entryType");

    public final StringPath externalCopyrightNotice = createString("externalCopyrightNotice");

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final StringPath observationCategory = createString("observationCategory");

    public final StringPath observationIdInForm = createString("observationIdInForm");

    public final StringPath observationRequiredInPanel = createString("observationRequiredInPanel");

    public final StringPath parentCode = createString("parentCode");

    public final StringPath parentId = createString("parentId");

    public final StringPath parentName = createString("parentName");

    public final StringPath path = createString("path");

    public final StringPath questionCardinality = createString("questionCardinality");

    public final StringPath relevanceEquation = createString("relevanceEquation");

    public final StringPath rootCode = createString("rootCode");

    public final StringPath seq = createString("seq");

    public final NumberPath<Integer> sequence = createNumber("sequence", Integer.class);

    public final StringPath skipLogicHelpText = createString("skipLogicHelpText");

    public final StringPath version = createString("version");

    public QPanel(String variable) {
        super(Panel.class, forVariable(variable));
    }

    public QPanel(Path<? extends Panel> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPanel(PathMetadata<?> metadata) {
        super(Panel.class, metadata);
    }

}

