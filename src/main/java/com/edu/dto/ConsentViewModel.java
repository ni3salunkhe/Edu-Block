package com.edu.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConsentViewModel {
    private List<ConsentDTO> pending;
    private List<ConsentDTO> active;
    private List<ConsentDTO> history;

    public ConsentViewModel() {
    }

    public ConsentViewModel(List<ConsentDTO> pending, List<ConsentDTO> active) {
        this.pending = pending;
        this.active = active;
        this.history = Collections.emptyList();
    }

    public ConsentViewModel(List<ConsentDTO> pending, List<ConsentDTO> active, List<ConsentDTO> history) {
        this.pending = pending;
        this.active = active;
        this.history = history;
    }

    public List<ConsentDTO> getPending() {
        return pending;
    }

    public void setPending(List<ConsentDTO> pending) {
        this.pending = pending;
    }

    public List<ConsentDTO> getActive() {
        return active;
    }

    public void setActive(List<ConsentDTO> active) {
        this.active = active;
    }

    public List<ConsentDTO> getHistory() {
        return history;
    }

    public void setHistory(List<ConsentDTO> history) {
        this.history = history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsentViewModel that = (ConsentViewModel) o;
        return Objects.equals(pending, that.pending) &&
               Objects.equals(active, that.active) &&
               Objects.equals(history, that.history);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pending, active, history);
    }

    @Override
    public String toString() {
        return "ConsentViewModel{" +
                "pending=" + pending +
                ", active=" + active +
                ", history=" + history +
                '}';
    }
}
