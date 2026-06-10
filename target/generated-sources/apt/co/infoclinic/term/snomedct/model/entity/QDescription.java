package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QDescription is a Querydsl query type for Description
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QDescription extends EntityPathBase<Description> {

    private static final long serialVersionUID = 1477656847L;

    public static final QDescription description = new QDescription("description");

    public final QComponent _super = new QComponent(this);

    //inherited
    public final BooleanPath active = _super.active;

    public final StringPath caseSignificanceId = createString("caseSignificanceId");

    public final StringPath conceptId = createString("conceptId");

    public final StringPath descriptionId = createString("descriptionId");

    //inherited
    public final StringPath effectiveTime = _super.effectiveTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath languageCode = createString("languageCode");

    //inherited
    public final StringPath moduleId = _super.moduleId;

    public final StringPath term = createString("term");

    public final StringPath typeId = createString("typeId");

    public QDescription(String variable) {
        super(Description.class, forVariable(variable));
    }

    public QDescription(Path<? extends Description> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDescription(PathMetadata<?> metadata) {
        super(Description.class, metadata);
    }

}

