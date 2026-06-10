package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QHierarchy is a Querydsl query type for Hierarchy
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QHierarchy extends EntityPathBase<Hierarchy> {

    private static final long serialVersionUID = 611783022L;

    public static final QHierarchy hierarchy = new QHierarchy("hierarchy");

    public final NumberPath<Integer> childrenCount = createNumber("childrenCount", Integer.class);

    public final StringPath code = createString("code");

    public final NumberPath<Integer> descendantCount = createNumber("descendantCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath parent = createString("parent");

    public final StringPath path = createString("path");

    public final StringPath preferredName = createString("preferredName");

    public final NumberPath<Integer> sequence = createNumber("sequence", Integer.class);

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public QHierarchy(String variable) {
        super(Hierarchy.class, forVariable(variable));
    }

    public QHierarchy(Path<? extends Hierarchy> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHierarchy(PathMetadata<?> metadata) {
        super(Hierarchy.class, metadata);
    }

}

