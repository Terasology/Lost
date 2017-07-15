/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.lost.journal.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.journal.BrowserJournalChapterHandler;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalEntryProducer;
import org.terasology.journal.JournalManager;
import org.terasology.journal.TimestampResolver;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.HTMLLikeParser;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.utilities.Assets;
import org.terasology.world.block.BlockManager;

import java.util.Arrays;
import java.util.Collection;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ItemPickupJournalIntegration extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ItemPickupJournalIntegration.class);
    @In
    private JournalManager journalManager;
    @In
    private PrefabManager prefabManager;
    @In
    private BlockManager blockManager;
    private String lostChapterId = "Lost";

    private ParagraphRenderStyle centerRenderStyle = new ParagraphRenderStyle() {
        @Override
        public HorizontalAlign getHorizontalAlignment() {
            return HorizontalAlign.CENTER;
        }
    };

    @Override
    public void preBegin() {

        BrowserJournalChapterHandler chapterHandler = new BrowserJournalChapterHandler();

        chapterHandler.registerJournalEntry("Exploration Log #112",
                Arrays.asList(
                        createTitleParagraph("Exploration Log #112"),
                        createTextParagraph("After five hours of trying to establish communication with base back at Narvij and nearby space " +
                                "stations, I’ve given up. The solar flares from Proxima Lateralus seem to have some sort of interference with" +
                                " the signals. The situation seems grim, my Antrum Sabre, which I used to create the wormhole, is now dysfunctional." +
                                " I believe it was impacted in the journey. With the wormhole creator destroyed, I have no way to go back home."),
                        createTextParagraph("The planet seems promising, as of yet. There is no sign of intelligent life. I do see some alien " +
                                "lifeform alike some of the animals back home. I should find some way to ensure survival. This might just be " +
                                "the last planet I set foot on.")
                ));

        chapterHandler.registerJournalEntry("Exploration Log #113",
                Arrays.asList(
                        createTitleParagraph("Exploration Log #113"),
                        createTextParagraph("After five hours of trying to establish communication with base back at Narvij and nearby space " +
                                "stations, I’ve given up. The solar flares from Proxima Lateralus seem to have some sort of interference with" +
                                " the signals. The situation seems grim, my Antrum Sabre, which I used to create the wormhole, is now dysfunctional." +
                                " I believe it was impacted in the journey. With the wormhole creator destroyed, I have no way to go back home."),
                        createTextParagraph("The planet seems promising, as of yet. There is no sign of intelligent life. I do see some alien " +
                                "lifeform alike some of the animals back home. I should find some way to ensure survival. This might just be " +
                                "the last planet I set foot on.")
                ));

        journalManager.registerJournalChapter(lostChapterId,
                Assets.getTextureRegion("Lost:journalIcons#WoodAndStone").get(),
                "Lost", chapterHandler);
    }

    private JournalEntryProducer createTimestampEntryProducer(String text) {
        return new JournalEntryProducer() {
            @Override
            public Collection<ParagraphData> produceParagraph(long date) {
                return Arrays.asList(
                        HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle, TimestampResolver.getJournalEntryDate(date)),
                        HTMLLikeParser.parseHTMLLikeParagraph(null,
                                text));
            }
        };
    }

    private ParagraphData createTextParagraph(String text) {
        return HTMLLikeParser.parseHTMLLikeParagraph(null, text);
    }

    private ParagraphData createTitleParagraph(String title) {
        return HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle, "<f engine:title>" + title + "</f>");
    }

    @ReceiveEvent
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef player,
                                   CharacterComponent characterComponent) {
        Prefab prefab = event.getNewItem().getParentPrefab();
        if (prefab != null) {
            ResourceUrn prefabUri = prefab.getUrn();
            if (prefabUri.equals(new ResourceUrn("Lost", "secretsBook"))) {
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log #112"));
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log #113"));
            }
        }
    }


}
