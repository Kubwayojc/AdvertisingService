package com.amazon.ata.advertising.service.businesslogic;

import com.amazon.ata.advertising.service.dao.ReadableDao;
import com.amazon.ata.advertising.service.model.*;
import com.amazon.ata.advertising.service.targeting.TargetingEvaluator;
import com.amazon.ata.advertising.service.targeting.TargetingGroup;

import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.C;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * This class is responsible for picking the advertisement to be rendered.
 */
public class AdvertisementSelectionLogic {

    private static final Logger LOG = LogManager.getLogger(AdvertisementSelectionLogic.class);

    private final ReadableDao<String, List<AdvertisementContent>> contentDao;
    private final ReadableDao<String, List<TargetingGroup>> targetingGroupDao;


    private Random random = new Random();

    /**
     * Constructor for AdvertisementSelectionLogic.
     *
     * @param contentDao        Source of advertising content.
     * @param targetingGroupDao Source of targeting groups for each advertising content.
     */
    @Inject
    public AdvertisementSelectionLogic(ReadableDao<String, List<AdvertisementContent>> contentDao,
                                       ReadableDao<String, List<TargetingGroup>> targetingGroupDao
    ) {
        this.contentDao = contentDao;
        this.targetingGroupDao = targetingGroupDao;

    }

    /**
     * Setter for Random class.
     *
     * @param random generates random number used to select advertisements.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Gets all of the content and metadata for the marketplace and determines which content can be shown.  Returns the
     * eligible content with the highest click through rate.  If no advertisement is available or eligible, returns an
     * EmptyGeneratedAdvertisement.
     *
     * @param customerId    - the customer to generate a custom advertisement for
     * @param marketplaceId - the id of the marketplace the advertisement will be rendered on
     * @return an advertisement customized for the customer id provided, or an empty advertisement if one could
     * not be generated.
     */

//    public GeneratedAdvertisement selectAdvertisement(String customerId, String marketplaceId) {
//
//        GeneratedAdvertisement generatedAdvertisement = new EmptyGeneratedAdvertisement();
//
//        if (StringUtils.isEmpty(marketplaceId)) {
//            LOG.warn("MarketplaceId cannot be null or empty. Returning empty ad.");
//
//        } else {

//            TargetingEvaluator targetingEvaluator = new TargetingEvaluator(new RequestContext(customerId, marketplaceId));
//            ExecutorService executorService = Executors.newCachedThreadPool();
//
//            TreeMap<TargetingGroup, Integer> treeMap = new TreeMap<>(Comparator.comparing(TargetingGroup::getClickThroughRate));
//
//            generatedAdvertisement = new GeneratedAdvertisement(contentDao.get(marketplaceId).stream()
//                    .map(content -> targetingGroupDao.get(content.getContentId()).stream()
//
//
////                    .sorted(Comparator.comparing(TargetingGroup::getClickThroughRate))
//
//                    .map(targetingEvaluator::evaluate)
//                    .anyMatch(TargetingPredicateResult::isTrue) ? content : null)
//                    .filter(Objects::nonNull)
//
//                    .findAny().get());
//        }
//
//
//
//
//
//
////        TargetingEvaluator targetingEvaluator = new TargetingEvaluator(new RequestContext(customerId, marketplaceId));
////        ExecutorService executorService = Executors.newCachedThreadPool();
////
////        generatedAdvertisement = new GeneratedAdvertisement(contentDao.get(marketplaceId).stream()
////                .map(content -> targetingGroupDao.get(content.getContentId()).stream()
////                        .sorted(Comparator.comparing(TargetingGroup::getClickThroughRate))
////                        .map(targetingEvaluator::evaluate)
////                        .anyMatch(TargetingPredicateResult::isTrue) ? content : null)
////                .filter(Objects::nonNull)
////                .findAny().get());
////    }
//
//
//
//
//        return generatedAdvertisement;
//    }
//}

//            final List<AdvertisementContent> contents = contentDao.get(marketplaceId);
//            contents.stream()



//                    .filter(content -> {
//                        List<TargetingGroup> group = targetingGroupDao.get(content.getContentId());
//                        group.sort(Comparator.comparing(TargetingGroup::getClickThroughRate));
//
//
//                        group.stream()
//                                .findFirst()
//                                .filter(targetingGroup -> targetingEvaluator.evaluate(targetingGroup).isTrue());
//
//                        return true;
//
//
//                            }
//
//
//
//
//                                );







//
//        }
//        return generatedAdvertisement;
//    }
//}





//          for LOOP: still not passing
//            for (AdvertisementContent content:contents) {
//                List<TargetingGroup> group = targetingGroupDao.get(content.getContentId());
//                group.sort(Comparator.comparing(TargetingGroup::getClickThroughRate).reversed());
//
//                for (TargetingGroup target : group){
//                    TargetingEvaluator targetingEvaluator =  new TargetingEvaluator(new RequestContext(customerId,marketplaceId));
//                    TargetingPredicateResult result = targetingEvaluator.evaluate(target);
//                    if(result.isTrue()){
//                        //generatedAdvertisement = new GeneratedAdvertisement(content);
//                        break;
//
//                    }
//
//                }
//                generatedAdvertisement = new GeneratedAdvertisement(content);
//            }
//
//
//
//        }
//
//        return generatedAdvertisement;
//    }
//}
//          original code:

    public GeneratedAdvertisement selectAdvertisement(String customerId, String marketplaceId) {
        GeneratedAdvertisement generatedAdvertisement = new EmptyGeneratedAdvertisement();
        if (StringUtils.isEmpty(marketplaceId)) {
            LOG.warn("MarketplaceId cannot be null or empty. Returning empty ad.");
        } else {
            final List<AdvertisementContent> contents = contentDao.get(marketplaceId);

            TreeMap<Double, TargetingGroup> treeMap = new TreeMap<>();
            for(AdvertisementContent content : contents) {
                List<TargetingGroup> groups = targetingGroupDao.get(content.getContentId());
                for(TargetingGroup group : groups) {
                    double click = group.getClickThroughRate();
                    treeMap.put(click, group);

                }

                Double max = treeMap.keySet().stream().max(Double::compare).get();
                for(TargetingGroup group : groups) {
                    if(group.getClickThroughRate() == max) {
                        generatedAdvertisement =  new GeneratedAdvertisement(content);
                    }
                }

            }


            }
        return generatedAdvertisement;

        }


    }

