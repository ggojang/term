package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QTransitiveClosure is a Querydsl query type for TransitiveClosure
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTransitiveClosure extends EntityPathBase<TransitiveClosure> {

    private static final long serialVersionUID = 1175049001L;

    public static final QTransitiveClosure transitiveClosure = new QTransitiveClosure("transitiveClosure");

    public final NumberPath<Integer> childrenCount = createNumber("childrenCount", Integer.class);

    public final StringPath conceptId = createString("conceptId");

    public final NumberPath<Integer> depth = createNumber("depth", Integer.class);

    public final NumberPath<Integer> descendantCount = createNumber("descendantCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath parentId = createString("parentId");

    public final StringPath path = createString("path");

    public final StringPath term = createString("term");

    public QTransitiveClosure(String variable) {
        super(TransitiveClosure.class, forVariable(variable));
    }

    public QTransitiveClosure(Path<? extends TransitiveClosure> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTransitiveClosure(PathMetadata<?> metadata) {
        super(TransitiveClosure.class, metadata);
    }

}

