package com.podcastapp

import com.podcastapp.errors.UnprocessableException
import com.podcastapp.rss.RssParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RssParserTest {
    private val xml = """<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
 <channel>
  <title>Test Show</title>
  <description>About testing</description>
  <language>en</language>
  <itunes:author>Tester</itunes:author>
  <itunes:category text="Technology"/>
  <item>
   <title>Episode 1</title>
   <guid>ep-1</guid>
   <pubDate>Mon, 06 Jan 2025 10:00:00 GMT</pubDate>
   <enclosure url="https://example.com/1.mp3" type="audio/mpeg" length="123"/>
   <itunes:duration>00:10:00</itunes:duration>
  </item>
  <item>
   <title>No audio</title>
   <guid>ep-2</guid>
  </item>
 </channel>
</rss>"""

    @Test fun parsesFeedAndSkipsItemsWithoutAudio() {
        val feed = RssParser.parse(xml.toByteArray())
        assertEquals("Test Show", feed.title)
        assertEquals(1, feed.episodes.size)
        assertEquals("ep-1", feed.episodes[0].guid)
        assertEquals("https://example.com/1.mp3", feed.episodes[0].audioUrl)
    }

    @Test fun rejectsGarbage() {
        assertFailsWith<UnprocessableException> { RssParser.parse("not xml".toByteArray()) }
    }
}
