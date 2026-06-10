package co.infoclinic.term.loinc.model.entity;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLinguisticVariants is a Querydsl query type for LinguisticVariants
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QLinguisticVariants extends EntityPathBase<LinguisticVariants> {

    private static final long serialVersionUID = 1900014554L;

    public static final QLinguisticVariants linguisticVariants = new QLinguisticVariants("linguisticVariants");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath isoCountry = createString("isoCountry");

    public final StringPath isoLang = createString("isoLang");

    public final StringPath lang = createString("lang");

    public final StringPath producer = createString("producer");

    public QLinguisticVariants(String variable) {
        super(LinguisticVariants.class, forVariable(variable));
    }

    public QLinguisticVariants(Path<? extends LinguisticVariants> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLinguisticVariants(PathMetadata<?> metadata) {
        super(LinguisticVariants.class, metadata);
    }

}

