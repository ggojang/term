package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLA is a Querydsl query type for LA
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLA extends EntityPathBase<LA> {

    private static final long serialVersionUID = 1653165116L;

    public static final QLA lA = new QLA("lA");

    public final StringPath answerStringID = createString("answerStringID");

    public final StringPath description = createString("description");

    public final StringPath displayText = createString("displayText");

    public final StringPath extCodeDisplayName = createString("extCodeDisplayName");

    public final StringPath extCodeID = createString("extCodeID");

    public final StringPath extCodeSystem = createString("extCodeSystem");

    public final StringPath extCodeSystemCopyrightNotice = createString("extCodeSystemCopyrightNotice");

    public final StringPath extCodeSystemVersion = createString("extCodeSystemVersion");

    public final StringPath extDefinedLACodeSystem = createString("extDefinedLACodeSystem");

    public final StringPath extDefinedLALink = createString("extDefinedLALink");

    public final StringPath extDefinedYn = createString("extDefinedYn");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath LAID = createString("LAID");

    public final StringPath LAName = createString("LAName");

    public final StringPath LAOid = createString("LAOid");

    public final StringPath localAnswerCode = createString("localAnswerCode");

    public final StringPath localAnswerCodeSystem = createString("localAnswerCodeSystem");

    public final StringPath score = createString("score");

    public final NumberPath<Integer> sequenceNumber = createNumber("sequenceNumber", Integer.class);

    public final StringPath subsequenceTextPrompt = createString("subsequenceTextPrompt");

    public QLA(String variable) {
        super(LA.class, forVariable(variable));
    }

    public QLA(Path<? extends LA> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLA(PathMetadata<?> metadata) {
        super(LA.class, metadata);
    }

}

