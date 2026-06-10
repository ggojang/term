package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QUserReferenceset is a Querydsl query type for UserReferenceset
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QUserReferenceset extends EntityPathBase<UserReferenceset> {

    private static final long serialVersionUID = 1535677295L;

    public static final QUserReferenceset userReferenceset = new QUserReferenceset("userReferenceset");

    public final QAbstractReferenceset _super = new QAbstractReferenceset(this);

    //inherited
    public final BooleanPath active = _super.active;

    //inherited
    public final StringPath effectiveTime = _super.effectiveTime;

    //inherited
    public final StringPath field1 = _super.field1;

    //inherited
    public final StringPath field2 = _super.field2;

    //inherited
    public final StringPath field3 = _super.field3;

    //inherited
    public final StringPath field4 = _super.field4;

    //inherited
    public final StringPath field5 = _super.field5;

    //inherited
    public final StringPath field6 = _super.field6;

    //inherited
    public final StringPath field7 = _super.field7;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath moduleId = _super.moduleId;

    //inherited
    public final StringPath referencedComponentId = _super.referencedComponentId;

    //inherited
    public final StringPath referencesetId = _super.referencesetId;

    //inherited
    public final StringPath refsetId = _super.refsetId;

    public QUserReferenceset(String variable) {
        super(UserReferenceset.class, forVariable(variable));
    }

    public QUserReferenceset(Path<? extends UserReferenceset> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserReferenceset(PathMetadata<?> metadata) {
        super(UserReferenceset.class, metadata);
    }

}

