package net.csibio.propro.algorithm.fitter;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.*;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Nico Wang
 * Time: 2018-12-19 17:07
 */
public class MultiGaussianFitter extends AbstractCurveFitter {

    public static final Logger logger = LoggerFactory.getLogger(MultiGaussianFitter.class);

    private final double[] initialGuess;
    private final int maxIter;
    private final int count;

    private static final Parametric FUNCTION = new Parametric() {
        @Override
        public double value(double x, double... p) {
            double v = 1.0D / 0.0;

            try {
                v = super.value(x, p);
            } catch (NotStrictlyPositiveException var7) {
                ;
            }

            return v;
        }

        @Override
        public double[] gradient(double x, double... p) {
            double[] v = new double[p.length];
            for (int index = 0; index < v.length; index++) {
                v[index] = 1.0D / 0.0;
            }
            try {
                v = super.gradient(x, p);
            } catch (NotStrictlyPositiveException var6) {
                ;
            }

            return v;
        }
    };

    private MultiGaussianFitter(double[] initialGuess, int maxIter, int count) {
        this.initialGuess = initialGuess;
        this.maxIter = maxIter;
        this.count = count;

    }

    public static MultiGaussianFitter create() {
        return new MultiGaussianFitter((double[]) null, 2147483647, 1);
    }

    public MultiGaussianFitter withStartPoint(double[] newStart) {
        return new MultiGaussianFitter((double[]) newStart.clone(), this.maxIter, this.count);
    }

    public MultiGaussianFitter withMaxIterations(int newMaxIter) {
        return new MultiGaussianFitter(this.initialGuess, newMaxIter, this.count);
    }

    public MultiGaussianFitter withCount(int count) {
        return new MultiGaussianFitter(this.initialGuess, this.maxIter, count);
    }

    @Override
    protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> observations) {
        int len = observations.size();
        double[] target = new double[len];
        double[] weights = new double[len];
        int i = 0;

        for (Iterator i$ = observations.iterator(); i$.hasNext(); ++i) {
            WeightedObservedPoint obs = (WeightedObservedPoint) i$.next();
            target[i] = obs.getY();
            weights[i] = obs.getWeight();
        }

        TheoreticalValuesFunction model = new TheoreticalValuesFunction(FUNCTION, observations);
        double[] startPoint = (new GaussParamGuesser(observations, this.count)).guess();
        return (new LeastSquaresBuilder()).maxEvaluations(2147483647).maxIterations(this.maxIter).start(startPoint).target(target).weight(new DiagonalMatrix(weights)).model(model.getModelFunction(), model.getModelFunctionJacobian()).build();

    }

    private static double gaussValue(double xMinusMean, double norm, double i2s2) {
        return norm * FastMath.exp(-xMinusMean * xMinusMean * i2s2);
    }

    //modified
    private static class Parametric implements ParametricUnivariateFunction {

        @Override
        public double value(double x, double... param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
            double result = 0;
            int count = param.length / 3;
            double[] tmpParam = new double[3];
            for (int i = 0; i < count; i++) {
                System.arraycopy(param, i * 3, tmpParam, 0, 3);
                result += valueSub(x, tmpParam);
            }
            return result;
        }

        private double valueSub(double x, double... param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
            this.validateParameters(param);
            double diff = x - param[1];
            double i2s2 = 1.0D / (2.0D * param[2] * param[2]);
            return gaussValue(diff, param[0], i2s2);
        }

        @Override
        public double[] gradient(double x, double... param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
            int count = param.length / 3;
            double[] tmpParam = new double[3];
            double[] tmpGradient;
            double[] result = new double[param.length];
            for (int i = 0; i < count; i++) {
                System.arraycopy(param, i * 3, tmpParam, 0, 3);
                tmpGradient = gradientSub(x, tmpParam);
                System.arraycopy(tmpGradient, 0, result, i * 3, 3);
            }
            return result;
        }

        private double[] gradientSub(double x, double... param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
            this.validateParameters(param);
            double norm = param[0];
            double diff = x - param[1];
            double sigma = param[2];
            double i2s2 = 1.0D / (2.0D * sigma * sigma);
            double n = gaussValue(diff, 1.0D, i2s2);
            double m = norm * n * 2.0D * i2s2 * diff;
            double s = m * diff / sigma;
            return new double[]{n, m, s};
        }

        private void validateParameters(double[] param) throws NullArgumentException, DimensionMismatchException, NotStrictlyPositiveException {
            if (param == null) {
                throw new NullArgumentException();
            } else if (param.length % 3 != 0) {
                throw new DimensionMismatchException(param.length, 3);
            } else if (param[2] <= 0.0D) {
                throw new NotStrictlyPositiveException(param[2]);
            }
        }

    }

    public enum Strategy {Count, Intensity, Gradient}

    public class GaussParamGuesser {
        private final List<Double> norm = new ArrayList<>();
        private final List<Double> mean = new ArrayList<>();
        private final List<Double> sigma = new ArrayList<>();
        //percentage of k gaining or losing percent threshold to define hidden peaks
        private final double margin = 0.5;
        private final double weight = 100;

        private final Strategy strategy = Strategy.Intensity;

        public GaussParamGuesser(Collection<WeightedObservedPoint> observations, int count) {
            if (observations == null) {
                throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY, new Object[0]);
            } else if (observations.size() < 3) {
                throw new NumberIsTooSmallException(observations.size(), 3, true);
            } else {
                double[] params;
                List<WeightedObservedPoint> sorted;
                switch (strategy) {
                    case Count:
                        sorted = this.sortObservations(observations);
                        params = this.basicGuess(sorted.toArray(new WeightedObservedPoint[0]));
                        logger.info(JSON.toJSON(params).toString());
                        for (int i = 0; i < count; i++) {
                            this.norm.add(params[0]);
                            this.mean.add((sorted.get(sorted.size() - 1).getX() - sorted.get(0).getX()) / count * (i + 1) + sorted.get(0).getX());
                            this.sigma.add(params[2]);
                        }
                        break;
                    case Intensity:
                        List<WeightedObservedPoint> tmpObs = new ArrayList<>();
                        boolean downHill = false;
                        WeightedObservedPoint lastPoint = new WeightedObservedPoint(0, 0, 0);

                        for (WeightedObservedPoint point : observations) {
                            if (point.getY() < lastPoint.getY()) {
                                downHill = true;
                            }
                            if (downHill && point.getY() > lastPoint.getY()) {
                                downHill = false;
                                sorted = this.sortObservations(tmpObs);
                                params = this.basicGuess(sorted.toArray(new WeightedObservedPoint[0]));
                                this.norm.add(params[0]);
                                this.mean.add(params[1]);
                                this.sigma.add(params[2]);
                                tmpObs = new ArrayList<>();
                                tmpObs.add(lastPoint);
                            }
                            tmpObs.add(point);
                            lastPoint = point;
                        }
                        sorted = this.sortObservations(tmpObs);
                        params = this.basicGuess(sorted.toArray(new WeightedObservedPoint[0]));
                        this.norm.add(params[0]);
                        this.mean.add(params[1]);
                        this.sigma.add(params[2]);
                        break;
                    case Gradient:
                        List<WeightedObservedPoint> obsList = Lists.newArrayList(observations);
                        List<Double> residualList = new ArrayList<>();
                        List<Double> localBoundaryX = new ArrayList<>();
                        //get residual
                        residualList.add(obsList.get(1).getY() - obsList.get(0).getY());
                        localBoundaryX.add(obsList.get(0).getX());
                        localBoundaryX.add(obsList.get(obsList.size() - 1).getX());
                        for (int i = 1; i < obsList.size() - 1; i++) {
                            residualList.add(obsList.get(i + 1).getY() - obsList.get(i).getY());
                            if (obsList.get(i).getY() == 0) {
                                localBoundaryX.add(obsList.get(i).getX());
                            }
                        }
                        //find local max index
                        double left, mid = 0, right = 0;
                        for (int i = 1; i < residualList.size() - 1; i++) {
                            left = residualList.get(i - 1);
                            mid = residualList.get(i);
                            right = residualList.get(i + 1);
                            //
                            if (left > 0) {
                                if (mid < 0) {
                                    this.mean.add(obsList.get(i).getX());
                                    this.norm.add(obsList.get(i).getY());
                                } else if (mid == 0 && right < 0) {
                                    this.mean.add((obsList.get(i).getX() + obsList.get(i + 1).getX()) / 2.0D);
                                    this.norm.add(obsList.get(i).getY());
                                    i++;
                                } else if (mid >= 0 && right > 0 && mid <= left * (1 - margin) && mid <= right * (1 - margin)) {
                                    this.mean.add(obsList.get(i).getX());
                                    this.norm.add(obsList.get(i).getY());
                                    localBoundaryX.add(obsList.get(i + 1).getX());
                                    i++;
                                }
                            } else if (left == 0) {
                                if (mid > 0) {
                                    localBoundaryX.add(obsList.get(i).getX());
                                } else if (mid < 0) {
                                    this.mean.add(obsList.get(i).getX());
                                    this.norm.add(obsList.get(i).getY());
                                }
                            } else if (left < 0) {
                                if (mid > 0) {
                                    localBoundaryX.add(obsList.get(i).getX());
                                } else if (mid == 0 && right > 0) {
                                    localBoundaryX.add((obsList.get(i).getX() + obsList.get(i + 1).getX()) / 2.0D);
                                    i++;
                                } else if (mid <= 0 && right < 0 && mid >= left * (1 - margin) && mid >= right * (1 - margin)) {
                                    localBoundaryX.add(obsList.get(i).getX());
                                    this.mean.add(obsList.get(i + 1).getX());
                                    this.norm.add(obsList.get(i + 1).getY());
                                    i++;
                                }
                            }
                        }
                        Collections.sort(localBoundaryX);
                        int i = 0;
                        for (int j = 0; i < this.mean.size(); j++) {
                            if (localBoundaryX.get(j) > this.mean.get(i)) {
                                this.sigma.add((localBoundaryX.get(j) - localBoundaryX.get(j - 1)) / (2.0D * FastMath.sqrt(2.0D * FastMath.log(2.0D))));
                                i++;
                            }
                        }
                        break;
                    default:
                        break;
                }
                logger.info("成功获得预估参数");
                logger.info("norm: " + JSON.toJSON(this.norm));
                logger.info("mean: " + JSON.toJSON(this.mean));
                logger.info("sigma: " + JSON.toJSON(this.sigma));
                logger.info(System.currentTimeMillis() + "");
            }
        }

        public double[] guess() {
            int length = this.norm.size();
            double[] result = new double[length * 3];
            for (int i = 0; i < length; i++) {
                result[3 * i] = this.norm.get(i);
                result[3 * i + 1] = this.mean.get(i);
                result[3 * i + 2] = this.sigma.get(i);
            }
            return result;
        }

        private List<WeightedObservedPoint> sortObservations(Collection<WeightedObservedPoint> unsorted) {
            List<WeightedObservedPoint> observations = new ArrayList(unsorted);
            Comparator<WeightedObservedPoint> cmp = new Comparator<WeightedObservedPoint>() {
                @Override
                public int compare(WeightedObservedPoint p1, WeightedObservedPoint p2) {
                    if (p1 == null && p2 == null) {
                        return 0;
                    } else if (p1 == null) {
                        return -1;
                    } else if (p2 == null) {
                        return 1;
                    } else {
                        int cmpX = Double.compare(p1.getX(), p2.getX());
                        if (cmpX < 0) {
                            return -1;
                        } else if (cmpX > 0) {
                            return 1;
                        } else {
                            int cmpY = Double.compare(p1.getY(), p2.getY());
                            if (cmpY < 0) {
                                return -1;
                            } else if (cmpY > 0) {
                                return 1;
                            } else {
                                int cmpW = Double.compare(p1.getWeight(), p2.getWeight());
                                if (cmpW < 0) {
                                    return -1;
                                } else {
                                    return cmpW > 0 ? 1 : 0;
                                }
                            }
                        }
                    }
                }
            };
            Collections.sort(observations, cmp);
            return observations;
        }

        private double[] basicGuess(WeightedObservedPoint[] points) {
            int maxYIdx = this.findMaxY(points);
            double n = points[maxYIdx].getY();
            double m = points[maxYIdx].getX();

            double fwhmApprox;
            double s;
            try {
                s = n + (m - n) / 2.0D;
                double fwhmX1 = this.interpolateXAtY(points, maxYIdx, -1, s);
                double fwhmX2 = this.interpolateXAtY(points, maxYIdx, 1, s);
                fwhmApprox = fwhmX2 - fwhmX1;
            } catch (OutOfRangeException var15) {
                fwhmApprox = points[points.length - 1].getX() - points[0].getX();
            }

            s = fwhmApprox / (2.0D * FastMath.sqrt(2.0D * FastMath.log(2.0D)));
            return new double[]{n, m, s};
        }

        private int findMaxY(WeightedObservedPoint[] points) {
            int maxYIdx = 0;

            for (int i = 1; i < points.length; ++i) {
                if (points[i].getY() > points[maxYIdx].getY()) {
                    maxYIdx = i;
                }
            }

            return maxYIdx;
        }

        private double interpolateXAtY(WeightedObservedPoint[] points, int startIdx, int idxStep, double y) throws OutOfRangeException {
            if (idxStep == 0) {
                throw new ZeroException();
            } else {
                WeightedObservedPoint[] twoPoints = this.getInterpolationPointsForY(points, startIdx, idxStep, y);
                WeightedObservedPoint p1 = twoPoints[0];
                WeightedObservedPoint p2 = twoPoints[1];
                if (p1.getY() == y) {
                    return p1.getX();
                } else {
                    return p2.getY() == y ? p2.getX() : p1.getX() + (y - p1.getY()) * (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
                }
            }
        }

        private WeightedObservedPoint[] getInterpolationPointsForY(WeightedObservedPoint[] points, int startIdx, int idxStep, double y) throws OutOfRangeException {
            if (idxStep == 0) {
                throw new ZeroException();
            } else {
                int i = startIdx;

                while (true) {
                    if (idxStep < 0) {
                        if (i + idxStep < 0) {
                            break;
                        }
                    } else if (i + idxStep >= points.length) {
                        break;
                    }

                    WeightedObservedPoint p1 = points[i];
                    WeightedObservedPoint p2 = points[i + idxStep];
                    if (this.isBetween(y, p1.getY(), p2.getY())) {
                        if (idxStep < 0) {
                            return new WeightedObservedPoint[]{p2, p1};
                        }

                        return new WeightedObservedPoint[]{p1, p2};
                    }

                    i += idxStep;
                }

                throw new OutOfRangeException(y, -1.0D / 0.0, 1.0D / 0.0);
            }
        }

        private boolean isBetween(double value, double boundary1, double boundary2) {
            return value >= boundary1 && value <= boundary2 || value >= boundary2 && value <= boundary1;
        }
    }
}



















