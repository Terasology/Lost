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
import org.terasology.journal.JournalAccessComponent;
import org.terasology.journal.JournalEntryProducer;
import org.terasology.journal.JournalManager;
import org.terasology.journal.TimestampResolver;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.nui.HorizontalAlign;
import org.terasology.registry.In;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.HTMLLikeParser;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.utilities.Assets;
import org.terasology.world.block.BlockManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LostJournalIntegration extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(LostJournalIntegration.class);
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

        chapterHandler.registerJournalEntry("Exploration Log #111",
                Arrays.asList(
                        createTitleParagraph("Exploration Log #111"),
                        createTextParagraph("Survey of Epsilon Draconis system is now complete. The system is concluded to be inhabitable " +
                                "after seven months of relentless exploration. However, planet 7: Scorpion Chapel, scouted by Ranger 729 is " +
                                "confirmed to possess huge amounts of metal ores and methane. The planet can be scavenged for resources but is " +
                                "still incapable for supporting life akin to other planets in the system."),
                        createTextParagraph("Iota Lateralus, a binary star planetary system, distant 3006 light years from Narvij, " +
                                "appears promising. Survey is about to begin in two days. Inspection of circumbinary planet 4 of the" +
                                " system- Rosseta Parabole, has been assigned to me. Mode of travel would be a regular triodal wormhole. " +
                                "Duration should be less than a month. Glad to be back scouting!<l><l>")
                ));

        chapterHandler.registerJournalEntry("Exploration Log #112",
                Arrays.asList(
                        createTitleParagraph("Exploration Log #112"),
                        createTextParagraph("After five hours of trying to establish communication with base back at Narvij and nearby space " +
                                "stations, I’ve given up. The solar flares from Proxima Lateralus seem to have some sort of interference with" +
                                " the signals. The situation seems grim, my Antrum Sabre, which I used to create the wormhole, is now dysfunctional." +
                                " I believe it was impacted in the journey. With the wormhole creator destroyed, I have no way to go back home."),
                        createTextParagraph("The planet seems promising, as of yet. There is no sign of intelligent life. I do see some alien " +
                                "lifeform alike some of the animals back home. I should find some way to ensure survival. This might just be " +
                                "the last planet I set foot on.<l><l>")
                ));

        chapterHandler.registerJournalEntry("Exploration Log #112",
                Arrays.asList(
                        createTitleParagraph("Exploration Log #112"),
                        createTextParagraph("After five hours of trying to establish communication with base back at Narvij and nearby space " +
                                "stations, I’ve given up. The solar flares from Proxima Lateralus seem to have some sort of interference with" +
                                " the signals. The situation seems grim, my Antrum Sabre, which I used to create the wormhole, is now dysfunctional." +
                                " I believe it was impacted in the journey. With the wormhole creator destroyed, I have no way to go back home."),
                        createTextParagraph("The planet seems promising, as of yet. There is no sign of intelligent life. I do see some alien " +
                                "lifeform alike some of the animals back home. I should find some way to ensure survival. This might just be " +
                                "the last planet I set foot on.<l><l>")
                ));

        chapterHandler.registerJournalEntry("Exploration Log #113",
                Arrays.asList(
                        createTitleParagraph("Exploration Log #113"),
                        createTextParagraph("I am certain of the fact that this planet was once home to intelligent life forms. There are clear " +
                                "signs. In front of a beautifully crafted hut was a well. Deep inside the well hidden in the water, was a treasure chest. " +
                                "Inside the chest, I’ve found what looks like a book of secrets.<l><l>" +
                                "It talks about a treasure, possibly one that can take me out of this planet. " +
                                "There are three parts that the treasure is made of. " +
                                "With the hut lying to the north of the well, the three elements of the treasure lie in the other three directions- " +
                                "East, South and West.<l><l>" +
                                "The clues are indeed difficult to decipher. However, I should hurry and look for whatever is it that I can find. " +
                                "This might be my only chance to make it back.<l><l>" +
                                "This would be a ground-breaking discovery back in the HQ. The search has finally yielded results. Is this " +
                                "civilization more advanced than ours? Why don’t I see anyone around? Did they get wiped out? Was it a natural " +
                                "calamity or did they bring a catastrophe onto themselves?<l><l>" +
                                "Why do they refer to the treasure as one that would bring misery? Is the treasure the reason for the collapse " +
                                "of the early civilization that existed here?<l><l>")
                ));

        chapterHandler.registerJournalEntry("Exploration Log- The Circle Of Life",
                Arrays.asList(
                        createTitleParagraph("Exploration Log- The Circle Of Life"),
                        createTextParagraph("The structure was gigantic and mammoth in size. A large stonehenge, so big that it’s " +
                                "construction is unfathomable. Inside the circle made by the huge pillars of stone lay a fortified room.<l><l>" +
                                "The intelligent life-form is far too advanced than I imagined. Placed away from the reach of any unintelligent " +
                                "creatures was another treasure chest. The whole room was locked by a door that had the password 18- " +
                                "the number of pillars in the stonehenge. Separated from the door by an intricately " +
                                "arranged system of rotating platforms that had lava underneath. " +
                                "Mistime a jump and you’re cooked meat. The treasure chest possessed the key- an Arkenstone. Along with the " +
                                "artifact, was also another book. The book says that this small stone is the most integral part of the Portal.<l><l>" +
                                "I’m starting to believe that the whole idea might actually be true. That the early civilization did have a " +
                                "piece of witchcraft and wizardry that allowed them to stitch a hole in the fabric of space and time.<l><l>")
                ));

        chapterHandler.registerJournalEntry("Exploration Log- Shattered Temple of Hope",
                Arrays.asList(
                        createTitleParagraph("Exploration Log- Shattered Temple of Hope"),
                        createTextParagraph("The tea was actually the letter T. Amidst the golden sand lay a T shaped entrance made of " +
                                "brown dirt, to a solemn temple. The password to the door was a reference to the brown dirt.<l><l>" +
                                "My awe for the level of advancement that the inhabitants of this planet had reached, " +
                                "has attained a new high. The tunnel led to a temple that possessed another chest at its end. " +
                                "The path to the treasure was however blocked by a series of fireball launchers and swinging blades. " +
                                "The treasure chest consisted of Shattered Plasma the element for the construction of the foundation of the portal.<l><l>" +
                                "I wonder how the portal might actually work. Would it be like my Antrum Sabre that became dysfunctional?<l><l>")
                ));

        chapterHandler.registerJournalEntry("Exploration Log- The Hunt for Truth",
                Arrays.asList(
                        createTitleParagraph("Exploration Log- The Hunt for Truth"),
                        createTextParagraph("A Pyramid! Looks the same from all four sides. The answer was 42- " +
                                "one less than the number of blocks that make one side of the pyramid.<l><l>" +
                                "The structure was huge, and constructed with delicate and trained expertise. The " +
                                "civilization that lived here did know a lot. One side of the pyramid had a door that " +
                                "let to a room that had another treasure chest. This treasure chest was protected by a " +
                                "series of swinging blades that blocked the way.<l><l>" +
                                "Made with such precision, that the seeker would need to time the sprint perfectly in order " +
                                "to reach the other side. Moreover, the return was more difficult than the approach.<l><l>" +
                                "The treasure consisted of the Facade of Truth, one of the three elements of the portal and instructions on how to use it.<l><l>")
                ));

        chapterHandler.registerJournalEntry("Exploration Log #114",
                Arrays.asList(
                        createTitleParagraph("Exploration Log #114"),
                        createTextParagraph("All the items for the portal have been found. Only thing that remains is to put everything together, and fire it up.<l><l>")
                ));

        journalManager.registerJournalChapter(lostChapterId,
                Assets.getTextureRegion("Lost:journalIcons#WoodAndStone").get(),
                "Lost", chapterHandler);
        logger.info("registered journal chapter");
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
        return HTMLLikeParser.parseHTMLLikeParagraph(centerRenderStyle, "<f engine:NotoSans-Regular-Title>" + title + "</f>");
    }

    @ReceiveEvent
    public void playerSpawned(OnPlayerSpawnedEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log #111"));
    }

    @ReceiveEvent
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef player,
                                   CharacterComponent characterComponent) {
        Prefab prefab = event.getNewItem().getParentPrefab();
        if (prefab != null) {
            ResourceUrn prefabUri = prefab.getUrn();
            if (prefabUri.equals(new ResourceUrn("WildAnimals", "meat"))) {
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log #112"));
            } else if (prefabUri.equals(new ResourceUrn("Lost", "secretsBook"))) {
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log #112"));
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log #113"));
            } else if (prefabUri.equals(new ResourceUrn("Lost", "arkenstoneBook"))) {
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log- The Circle Of Life"));
                sendOverallJourneyEvent(player);
            } else if (prefabUri.equals(new ResourceUrn("Lost", "facadeOfTruthBook"))) {
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log- The Hunt for Truth"));
                sendOverallJourneyEvent(player);
            } else if (prefabUri.equals(new ResourceUrn("Lost", "shatteredPlasmaBook"))) {
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log- Shattered Temple of Hope"));
                sendOverallJourneyEvent(player);
            }
        }
    }

    private void sendOverallJourneyEvent(EntityRef player) {
        JournalAccessComponent journalAccessComponent = player.getComponent(JournalAccessComponent.class);
        List<String> lostJournalEntries = journalAccessComponent.discoveredJournalEntries.get(lostChapterId);
        int flag = 0;
        for (String lostJournalEntry : lostJournalEntries) {
            if (lostJournalEntry.contains("Exploration Log- The Circle Of Life") ||
                    lostJournalEntry.contains("Exploration Log- The Hunt for Truth") ||
                    lostJournalEntry.contains("Exploration Log- Shattered Temple of Hope")) {
                flag += 1;
            }

            if (flag == 3) {
                player.send(new DiscoveredNewJournalEntry(lostChapterId, "Exploration Log #114"));
            }
        }
    }
}
