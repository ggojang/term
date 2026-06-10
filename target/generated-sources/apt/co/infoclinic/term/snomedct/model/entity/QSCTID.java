package co.infoclinic.term.snomedct.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QSCTID is a Querydsl query type for SCTID
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QSCTID extends EntityPathBase<SCTID> {

    private static final long serialVersionUID = 962465682L;

    public static final QSCTID sCTID = new QSCTID("sCTID");

    public final NumberPath<Integer> componentType = createNumber("componentType", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QSCTID(String variable) {
        super(SCTID.class, forVariable(variable));
    }

    public QSCTID(Path<? extends SCTID> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSCTID(PathMetadata<?> metadata) {
        super(SCTID.class, metadata);
    }

}

