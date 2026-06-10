package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QStatedRelationship is a Querydsl query type for StatedRelationship
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QStatedRelationship extends EntityPathBase<StatedRelationship> {

    private static final long serialVersionUID = 2120956760L;

    public static final QStatedRelationship statedRelationship = new QStatedRelationship("statedRelationship");

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

    public QStatedRelationship(String variable) {
        super(StatedRelationship.class, forVariable(variable));
    }

    public QStatedRelationship(Path<? extends StatedRelationship> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStatedRelationship(PathMetadata<?> metadata) {
        super(StatedRelationship.class, metadata);
    }

}

