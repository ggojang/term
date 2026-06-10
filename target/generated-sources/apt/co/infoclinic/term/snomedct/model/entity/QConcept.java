package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QConcept is a Querydsl query type for Concept
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QConcept extends EntityPathBase<Concept> {

    private static final long serialVersionUID = 1480898811L;

    public static final QConcept concept = new QConcept("concept");

    public final QComponent _super = new QComponent(this);

    //inherited
    public final BooleanPath active = _super.active;

    public final StringPath conceptId = createString("conceptId");

    public final StringPath definitionStatusId = createString("definitionStatusId");

    //inherited
    public final StringPath effectiveTime = _super.effectiveTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath moduleId = _super.moduleId;

    public QConcept(String variable) {
        super(Concept.class, forVariable(variable));
    }

    public QConcept(Path<? extends Concept> path) {
        super(path.getType(), path.getMetadata());
    }

    public QConcept(PathMetadata<?> metadata) {
        super(Concept.class, metadata);
    }

}

