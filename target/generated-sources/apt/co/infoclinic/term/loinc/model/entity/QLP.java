package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLP is a Querydsl query type for LP
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLP extends EntityPathBase<LP> {

    private static final long serialVersionUID = 1653165131L;

    public static final QLP lP = new QLP("lP");

    public final StringPath partDisplayName = createString("partDisplayName");

    public final StringPath partName = createString("partName");

    public final StringPath partNumber = createString("partNumber");

    public final StringPath partTypeName = createString("partTypeName");

    public final StringPath status = createString("status");

    public QLP(String variable) {
        super(LP.class, forVariable(variable));
    }

    public QLP(Path<? extends LP> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLP(PathMetadata<?> metadata) {
        super(LP.class, metadata);
    }

}

