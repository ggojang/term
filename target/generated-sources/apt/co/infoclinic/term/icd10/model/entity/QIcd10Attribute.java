package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QIcd10Attribute is a Querydsl query type for Icd10Attribute
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QIcd10Attribute extends EntityPathBase<Icd10Attribute> {

    private static final long serialVersionUID = -801203064L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIcd10Attribute icd10Attribute = new QIcd10Attribute("icd10Attribute");

    public final StringPath attribute = createString("attribute");

    public final StringPath attributeContainer = createString("attributeContainer");

    public final QIcd10Entity icd10Entity;

    public final StringPath id = createString("id");

    public final NumberPath<Integer> seq = createNumber("seq", Integer.class);

    public final StringPath value = createString("value");

    public QIcd10Attribute(String variable) {
        this(Icd10Attribute.class, forVariable(variable), INITS);
    }

    public QIcd10Attribute(Path<? extends Icd10Attribute> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QIcd10Attribute(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QIcd10Attribute(PathMetadata<?> metadata, PathInits inits) {
        this(Icd10Attribute.class, metadata, inits);
    }

    public QIcd10Attribute(Class<? extends Icd10Attribute> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.icd10Entity = inits.isInitialized("icd10Entity") ? new QIcd10Entity(forProperty("icd10Entity"), inits.get("icd10Entity")) : null;
    }

}

