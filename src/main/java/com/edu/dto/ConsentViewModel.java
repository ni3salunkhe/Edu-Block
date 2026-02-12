package com.edu.dto;

import java.util.List;
import java.util.Objects;

public class ConsentViewModel {
    private List<ConsentDTO> pending;
    private List<ConsentDTO> active;

    public ConsentViewModel() {
    }

    public ConsentViewModel(List<ConsentDTO> pending, List<ConsentDTO> active) {
        this.pending = pending;
        this.active = active;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsentViewModel that = (ConsentViewModel) o;
        return Objects.equals(pending, that.pending) &&
               Objects.equals(active, that.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pending, active);
    }

    @Override
    public String toString() {
        return "ConsentViewModel{" +
                "pending=" + pending +
                ", active=" + active +
                '}';
    }
}
