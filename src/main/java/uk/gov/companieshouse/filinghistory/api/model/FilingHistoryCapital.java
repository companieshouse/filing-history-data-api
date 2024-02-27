package uk.gov.companieshouse.filinghistory.api.model;

import java.util.Objects;

public class FilingHistoryCapital {

    private String currency;
    private String figure;
    private String date;

    public String getCurrency() {
        return currency;
    }

    public FilingHistoryCapital currency(String currency) {
        this.currency = currency;
        return this;
    }

    public String getFigure() {
        return figure;
    }

    public FilingHistoryCapital figure(String figure) {
        this.figure = figure;
        return this;
    }

    public String getDate() {
        return date;
    }

    public FilingHistoryCapital date(String date) {
        this.date = date;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilingHistoryCapital that = (FilingHistoryCapital) o;
        return Objects.equals(currency, that.currency) && Objects.equals(figure,
                that.figure) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, figure, date);
    }

    @Override
    public String toString() {
        return "FilingHistoryCapital{" +
                "currency='" + currency + '\'' +
                ", figure='" + figure + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
