package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.exceptions.PageIndexingException;
import searchengine.mapper.SiteMapper;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;
import searchengine.model.repositories.LemmaRepository;
import searchengine.model.repositories.PageRepository;
import searchengine.model.repositories.SiteEntityRepository;
import searchengine.model.services.SiteService;
import searchengine.web.services.dto.statistics.DetailedStatisticsItem;

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.List;

import static searchengine.core.utils.HtmlUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SitesList sites;
    private final SiteEntityRepository siteEntityRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteMapper siteMapper;

    @Override
    public SiteEntity getSiteEntityByLink(String link) throws PageIndexingException {
        String baseUrl;
        Site site = null;
        try{
            baseUrl = getBaseUrl(link);
            for(Site s : sites.getSites()){
                if(equalsHosts(s.getUrl(), baseUrl)){
                    site = s;
                }
            }
        } catch (MalformedURLException e) {
            throw new PageIndexingException(link, "Адрес ссылки введен неверно.");
        }
        if(site == null) {
            throw new PageIndexingException(link , "Сайт по ссылке отсутсвует в списке индексируемых сайтов", link);
        }
        try {
            return findByUrl(normalizeUrl(site.getUrl()));
        } catch (NoFoundEntityException e){
            log.warn("Ошибка: {}", e.getMessage());
            log.info("Запись сайта [{}] в БД" , site.getUrl());
            return create(siteMapper.dtoToEntity(site));
        }
    }

    @Override
    public SiteEntity findSite(Site site) {
        return findByUrl(normalizeUrl(site.getUrl()));
    }

    @Override
    @Transactional
    public SiteEntity create(Site site) {
        return siteEntityRepository.save(siteMapper.dtoToEntity(site));
    }

    @Override
    public List<SiteEntity> findByStatus(StatusType statusType) {
        return siteEntityRepository.findByStatus(statusType);
    }

    @Override
    public SiteEntity findById(Long id) {
        return siteEntityRepository.findById(id).orElseThrow(
                () -> new NoFoundEntityException("Сайт с ID: ", id, " не сохраненен в базе данных"));
    }

    @Override
    public List<SiteEntity> findAll() {
        return siteEntityRepository.findAll();
    }

    @Override
    public SiteEntity findByUrl(String url) {
        return siteEntityRepository.findByUrl(url).orElseThrow(
                () -> new NoFoundEntityException("Сайт с адресом: ", url, " не сохранен в базе данных"));
    }

    @Override
    public int count() {
        return Math.toIntExact(siteEntityRepository.count());
    }

    @Override
    @Transactional
    public SiteEntity create(SiteEntity entity) {
        return siteEntityRepository.save(entity);
    }

    @Override
    public boolean existsByUrl(String url) {
        return siteEntityRepository.existsByUrl(url);
    }

    @Override
    @Transactional
    public SiteEntity updateStatus(SiteEntity siteEntity){
        if(siteEntity.getStatus().equals(StatusType.FAILED)){
            siteEntityRepository.updateErrorStatus(
                    siteEntity.getId(),
                    siteEntity.getStatus(),
                    Instant.now(),
                    siteEntity.getLastError());
        }
        siteEntityRepository.updateStatus(siteEntity.getId(), siteEntity.getStatus(), Instant.now());
        return findById(siteEntity.getId());
    }

    @Override
    public DetailedStatisticsItem getDetailedStatisticItemBySite(Site site){
        DetailedStatisticsItem item;
        if(!existsByUrl(normalizeUrl(site.getUrl()))){
            item = siteMapper.noIndexedDataToDetailed(site);
        } else {
            SiteEntity siteEntity = findByUrl(normalizeUrl(site.getUrl()));
            item = siteMapper.entityToDetailedStatisticResult(siteEntity);
            item.setPages(pageRepository.countBySite(siteEntity));
            item.setLemmas(lemmaRepository.countBySite(siteEntity));
        }
        return item;
    }

    @Override
    public List<DetailedStatisticsItem> getDetailedStatisticItems(SitesList sites){
        return sites.getSites().stream().map(this::getDetailedStatisticItemBySite).toList();
    }
}
