package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QInferredRelationship is a Querydsl query type for InferredRelationship
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QInferredRelationship extends EntityPathBase<InferredRelationship> {

    private static final long serialVersionUID = 1107223464L;

    public static final QInferredRelationship inferredRelationship = new QInferredRelationship("inferredRelationship");

    public final QRelationship _super = new QRelationship(this);

    //inherited
    public final BooleanPath active = _super.active;

    //inherited
    public final StringPath characteristicTypeId = _super.characteristicTypeId;

    //inherited
    public final StringPath destinationId = _super.destinationId;

    //inherited
    public final StringPath effectiveTime = _super.effectiveTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath modifierId = _super.modifierId;

    //inherited
    public final StringPath moduleId = _super.moduleId;

    //inherited
    public final StringPath relationshipGroup = _super.relationshipGroup;

    //inherited
    public final StringPath relationshipId = _super.relationshipId;

    //inherited
    public final StringPath sourceId = _super.sourceId;

    //inherited
    public final StringPath typeId = _super.typeId;

    public QInferredRelationship(String variable) {
        super(InferredRelationship.class, forVariable(variable));
    }

    public QInferredRelationship(Path<? extends InferredRelationship> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInferredRelationship(PathMetadata<?> metadata) {
        super(InferredRelationship.class, metadata);
    }

}

