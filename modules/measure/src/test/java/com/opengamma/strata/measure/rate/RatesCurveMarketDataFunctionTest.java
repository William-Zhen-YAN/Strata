/*
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.measure.rate;

import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static com.opengamma.strata.collect.TestHelper.date;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.calc.marketdata.MarketDataConfig;
import com.opengamma.strata.calc.marketdata.MarketDataRequirements;
import com.opengamma.strata.data.ObservableSource;
import com.opengamma.strata.data.scenario.ImmutableScenarioMarketData;
import com.opengamma.strata.data.scenario.MarketDataBox;
import com.opengamma.strata.data.scenario.ScenarioMarketData;
import com.opengamma.strata.market.curve.ConstantCurve;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.curve.CurveGroupName;
import com.opengamma.strata.market.curve.CurveName;
import com.opengamma.strata.market.curve.RatesCurveGroup;
import com.opengamma.strata.market.curve.RatesCurveGroupId;
import com.opengamma.strata.market.curve.RatesCurveId;

/**
 * Test {@link RatesCurveMarketDataFunction}.
 */
@Test
public class RatesCurveMarketDataFunctionTest {

  private static final ReferenceData REF_DATA = ReferenceData.standard();
  private static final LocalDate VAL_DATE = date(2011, 3, 8);
  private static final CurveGroupName GROUP_NAME = CurveGroupName.of("Group");
  private static final CurveName CURVE_NAME1 = CurveName.of("Name1");
  private static final CurveName CURVE_NAME2 = CurveName.of("Name2");
  private static final ObservableSource OBS_SOURCE = ObservableSource.of("Vendor");

  //-------------------------------------------------------------------------
  public void test_singleCurve() {
    Curve curve = ConstantCurve.of(CURVE_NAME1, (double) 1);
    RatesCurveId curveId1 = RatesCurveId.of(GROUP_NAME, CURVE_NAME1, OBS_SOURCE);
    RatesCurveId curveId2 = RatesCurveId.of(GROUP_NAME, CURVE_NAME2, OBS_SOURCE);
    RatesCurveGroupId groupId = RatesCurveGroupId.of(GROUP_NAME, OBS_SOURCE);
    RatesCurveGroup curveGroup = RatesCurveGroup.of(
        GROUP_NAME,
        ImmutableMap.of(Currency.AUD, curve),
        ImmutableMap.of());
    ScenarioMarketData marketData = ImmutableScenarioMarketData.builder(VAL_DATE).addValue(groupId, curveGroup).build();

    RatesCurveMarketDataFunction test = new RatesCurveMarketDataFunction();
    MarketDataRequirements reqs = test.requirements(curveId1, MarketDataConfig.empty());
    assertEquals(reqs.getNonObservables(), ImmutableSet.of(groupId));
    MarketDataBox<Curve> result = test.build(curveId1, MarketDataConfig.empty(), marketData, REF_DATA);
    assertEquals(result, MarketDataBox.ofSingleValue(curve));
    assertThrowsIllegalArg(() -> test.build(curveId2, MarketDataConfig.empty(), marketData, REF_DATA));
  }

  public void test_multipleCurves() {
    Curve curve1 = ConstantCurve.of(CURVE_NAME1, (double) 1);
    Curve curve2 = ConstantCurve.of(CURVE_NAME2, (double) 2);
    RatesCurveId curveId1 = RatesCurveId.of(GROUP_NAME, CURVE_NAME1);
    RatesCurveId curveId2 = RatesCurveId.of(GROUP_NAME, CURVE_NAME2);
    RatesCurveGroupId groupId = RatesCurveGroupId.of(GROUP_NAME);
    RatesCurveGroup curveGroup = RatesCurveGroup.of(
        GROUP_NAME,
        ImmutableMap.of(Currency.AUD, curve1, Currency.GBP, curve2),
        ImmutableMap.of());
    ScenarioMarketData marketData = ImmutableScenarioMarketData.builder(VAL_DATE).addValue(groupId, curveGroup).build();

    RatesCurveMarketDataFunction test = new RatesCurveMarketDataFunction();
    MarketDataBox<Curve> result1 = test.build(curveId1, MarketDataConfig.empty(), marketData, REF_DATA);
    assertEquals(result1, MarketDataBox.ofSingleValue(curve1));
    MarketDataBox<Curve> result2 = test.build(curveId2, MarketDataConfig.empty(), marketData, REF_DATA);
    assertEquals(result2, MarketDataBox.ofSingleValue(curve2));
  }

}