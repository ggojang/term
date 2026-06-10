package co.infoclinic.term.icd10.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QIcd10EntityId is a Querydsl query type for Icd10EntityId
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QIcd10EntityId extends BeanPath<Icd10EntityId> {

    private static final long serialVersionUID = 2028052722L;

    public static final QIcd10EntityId icd10EntityId = new QIcd10EntityId("icd10EntityId");

    public final StringPath entity = createString("entity");

    public final StringPath id = createString("id");

    public QIcd10EntityId(String variable) {
        super(Icd10EntityId.class, forVariable(variable));
    }

    public QIcd10EntityId(Path<? extends Icd10EntityId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIcd10EntityId(PathMetadata<?> metadata) {
        super(Icd10EntityId.class, metadata);
    }

}

