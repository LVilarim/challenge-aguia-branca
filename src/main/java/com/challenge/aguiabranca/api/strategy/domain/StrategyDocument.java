package com.challenge.aguiabranca.api.strategy.domain;

import com.challenge.aguiabranca.api.common.error.BusinessRuleException;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("strategies")
@CompoundIndexes({
        @CompoundIndex(name = "only_one_active", def = "{'status': 1}", unique = true,
                partialFilter = "{'status': 'ATIVA'}"),
        @CompoundIndex(name = "category_valid_from_idx", def = "{'category': 1, 'validFrom': -1}")
})
public class StrategyDocument {
    @Id private String id;
    @Indexed private String category;
    private String campaign;
    private String description;
    @Indexed private StrategyStatus status = StrategyStatus.RASCUNHO;
    private Instant validFrom;
    private Instant validUntil;
    @Version private Long version;
    private String createdBy;
    private String updatedBy;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    public StrategyDocument() {}

    public StrategyDocument(String category, String campaign, String description, Instant validFrom,
                            Instant validUntil, String actor) {
        changeDetails(category, campaign, description, validFrom, validUntil, actor);
        this.createdBy = actor;
        this.status = StrategyStatus.RASCUNHO;
    }

    public void changeDetails(String category, String campaign, String description, Instant validFrom,
                              Instant validUntil, String actor) {
        if (validFrom != null && validUntil != null && validUntil.isBefore(validFrom)) {
            throw new BusinessRuleException("INVALID_DATE_RANGE", "A data final não pode anteceder a inicial.");
        }
        if (status == StrategyStatus.ARQUIVADA) {
            throw new BusinessRuleException("INVALID_STATE_TRANSITION", "Estratégia arquivada não pode ser editada.");
        }
        this.category = category.trim();
        this.campaign = campaign.trim();
        this.description = description.trim();
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.updatedBy = actor;
    }

    public void activate(Instant effectiveAt, String actor) {
        if (status != StrategyStatus.RASCUNHO && status != StrategyStatus.ENCERRADA) {
            throw new BusinessRuleException("INVALID_STATE_TRANSITION", "A estratégia não pode ser ativada neste estado.");
        }
        status = StrategyStatus.ATIVA;
        validFrom = effectiveAt;
        validUntil = null;
        updatedBy = actor;
    }

    public void close(Instant effectiveAt, String actor) {
        if (status != StrategyStatus.ATIVA) {
            throw new BusinessRuleException("INVALID_STATE_TRANSITION", "Somente estratégia ativa pode ser encerrada.");
        }
        if (validFrom != null && effectiveAt.isBefore(validFrom)) {
            throw new BusinessRuleException("INVALID_DATE_RANGE", "O encerramento não pode anteceder a ativação.");
        }
        status = StrategyStatus.ENCERRADA;
        validUntil = effectiveAt;
        updatedBy = actor;
    }

    public void archive(String actor) {
        if (status == StrategyStatus.ATIVA) {
            throw new BusinessRuleException("INVALID_STATE_TRANSITION", "Encerre a estratégia antes de arquivá-la.");
        }
        status = StrategyStatus.ARQUIVADA;
        updatedBy = actor;
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getCampaign() { return campaign; }
    public String getDescription() { return description; }
    public StrategyStatus getStatus() { return status; }
    public Instant getValidFrom() { return validFrom; }
    public Instant getValidUntil() { return validUntil; }
    public Long getVersion() { return version; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

