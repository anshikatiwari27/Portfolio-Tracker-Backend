package com.PortfolioTracker.PortfolioTracker.Entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@Getter
@Setter
public class Stocks {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid", unique = true)
    private UUID uuid;

    @Column(name = "stockname",unique = true, length = 50)
    private String stockname;

    @Column(name = "ticker", length = 255)
    private String ticker;

    public String getStockname() {
        return stockname;
    }

    public void setStockname(String stockname) {
        this.stockname = stockname;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
