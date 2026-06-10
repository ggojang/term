package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QRelationship is a Querydsl query type for Relationship
 */
@Generated("com.mysema.query.codegen.SupertypeSerializer")
public class QRelationship extends EntityPathBase<Relationship> {

    private static final long serialVersionUID = 222190469L;

    public static final QRelationship relationship = new QRelationship("relationship");

    public final QComponent _super = new QComponent(this);

    //inherited
    public final BooleanPath active = _super.active;

    public final StringPath characteristicTypeId = createString("characteristicTypeId");

    public final StringPath destinationId = createString("destinationId");

    //inherited
    public final StringPath effectiveTime = _super.effectiveTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath modifierId = createString("modifierId");

    //inherited
    public final StringPath moduleId = _super.moduleId;

    public final StringPath relationshipGroup = createString("relationshipGroup");

    public final StringPath relationshipId = createString("relationshipId");

    public final StringPath sourceId = createString("sourceId");

    public final StringPath typeId = createString("typeId");

    public QRelationship(String variable) {
        super(Relationship.class, forVariable(variable));
    }

    public QRelationship(Path<? extends Relationship> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRelationship(PathMetadata<?> metadata) {
        super(Relationship.class, metadata);
    }

}

