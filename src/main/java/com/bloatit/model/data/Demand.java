package com.bloatit.model.data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Demand extends UserContent {
	public enum State {
		CONSTRUCTING, VALIDATED, DEVELOPING, DEVELOPED, ACCEPTED, REJECTED;
	}

	@Basic(optional = false)
	private State state;
	@OneToOne(mappedBy = "demand", optional = false)
	private Draft currentDraft;
	@OneToMany(mappedBy = "demand")
	private Set<Draft> drafts = new HashSet<Draft>(0); // a popularity sorted
	@OneToMany(mappedBy = "demand")
	private Set<Offer> offers = new HashSet<Offer>(0);
	@OneToMany(mappedBy = "demand")
	private Set<Transaction> contributions = new HashSet<Transaction>(0);

	protected Demand() {
		super();
	}

	public Demand(Member author, LocalizedText title, LocalizedText description, LocalizedText specification) {
		super(author);
		state = State.CONSTRUCTING;
		currentDraft = new Draft(author, this, title, description, specification);
		drafts.add(currentDraft);
	}

	public void addDraft(Member author, LocalizedText title, LocalizedText description, LocalizedText specification) throws Throwable {
		if (state != State.CONSTRUCTING) {
			throw (new Throwable("Demand is no longuer in construction mode."));
		}
		drafts.add(new Draft(author, this, title, description, specification));
	}

	public void addOffer() {
		// TODO
	}

	public void addContribution(Member member, BigDecimal amount) throws Throwable {
		if (amount.compareTo(new BigDecimal("0")) <= 0) {
			throw (new Throwable("Amount must be non null and positive."));
		}
		contributions.add(new Transaction(member, amount));
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

	public Draft getCurrentDraft() {
		return currentDraft;
	}

	public Set<Draft> getDrafts() {
		return drafts;
	}

	public Set<Offer> getOffers() {
		return offers;
	}

	public Set<Transaction> getContributions() {
		return contributions;
	}
	
	// ======================================================================
	// For hibernate mapping
	// ======================================================================
	
	protected void setCurrentDraft(Draft currentDraft) {
    	this.currentDraft = currentDraft;
    }

	protected void setDrafts(Set<Draft> drafts) {
    	this.drafts = drafts;
    }

	protected void setOffers(Set<Offer> offers) {
    	this.offers = offers;
    }

	protected void setContributions(Set<Transaction> contributions) {
    	this.contributions = contributions;
    }
}
