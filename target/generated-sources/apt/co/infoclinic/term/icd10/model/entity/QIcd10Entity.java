package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QIcd10Entity is a Querydsl query type for Icd10Entity
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QIcd10Entity extends EntityPathBase<Icd10Entity> {

    private static final long serialVersionUID = -1325262473L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIcd10Entity icd10Entity = new QIcd10Entity("icd10Entity");

    public final QIcd10Attribute icd10Attribute;

    public final ListPath<Icd10Property, QIcd10Property> icd10Properties = this.<Icd10Property, QIcd10Property>createList("icd10Properties", Icd10Property.class, QIcd10Property.class, PathInits.DIRECT2);

    public final QIcd10EntityId id;

    public final StringPath name = createString("name");

    public final StringPath parent = createString("parent");

    public final StringPath type = createString("type");

    public QIcd10Entity(String variable) {
        this(Icd10Entity.class, forVariable(variable), INITS);
    }

    public QIcd10Entity(Path<? extends Icd10Entity> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QIcd10Entity(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QIcd10Entity(PathMetadata<?> metadata, PathInits inits) {
        this(Icd10Entity.class, metadata, inits);
    }

    public QIcd10Entity(Class<? extends Icd10Entity> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.icd10Attribute = inits.isInitialized("icd10Attribute") ? new QIcd10Attribute(forProperty("icd10Attribute"), inits.get("icd10Attribute")) : null;
        this.id = inits.isInitialized("id") ? new QIcd10EntityId(forProperty("id")) : null;
    }

}

