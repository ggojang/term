package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QIcd10Property is a Querydsl query type for Icd10Property
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QIcd10Property extends EntityPathBase<Icd10Property> {

    private static final long serialVersionUID = -1157955991L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIcd10Property icd10Property = new QIcd10Property("icd10Property");

    public final StringPath entity = createString("entity");

    public final QIcd10Entity icd10Entity;

    public final StringPath id = createString("id");

    public final StringPath language = createString("language");

    public final StringPath name = createString("name");

    public final StringPath property = createString("property");

    public final NumberPath<Integer> seq = createNumber("seq", Integer.class);

    public final StringPath type = createString("type");

    public final StringPath value = createString("value");

    public QIcd10Property(String variable) {
        this(Icd10Property.class, forVariable(variable), INITS);
    }

    public QIcd10Property(Path<? extends Icd10Property> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QIcd10Property(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QIcd10Property(PathMetadata<?> metadata, PathInits inits) {
        this(Icd10Property.class, metadata, inits);
    }

    public QIcd10Property(Class<? extends Icd10Property> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.icd10Entity = inits.isInitialized("icd10Entity") ? new QIcd10Entity(forProperty("icd10Entity"), inits.get("icd10Entity")) : null;
    }

}

