#!/usr/bin/env python3
"""
Golem XIV - DDGS Search HTTP Service
Copyright (C) 2025  Kazimierz Pogoda / Xemantic

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
"""

from fastapi import FastAPI, Query, HTTPException
from ddgs import DDGS
from typing import Optional, List, Dict, Any
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Golem XIV DDGS Search Service",
    description="Web search service using DDGS (Dux Distributed Global Search) metasearch library",
    version="1.0.0"
)


@app.get("/health")
def health() -> Dict[str, str]:
    """Health check endpoint."""
    return {"status": "ok", "service": "ddgs-search"}


@app.get("/search")
def search(
    query: str = Query(..., description="Search query"),
    region: str = Query("us-en", description="Search region (e.g., us-en, uk-en, ru-ru)"),
    safesearch: str = Query("moderate", description="Safe search level: on, moderate, off"),
    timelimit: Optional[str] = Query(None, description="Time filter: d (day), w (week), m (month), y (year)"),
    max_results: int = Query(10, ge=1, le=100, description="Maximum number of results"),
    page: int = Query(1, ge=1, description="Page number"),
    backend: str = Query("auto", description="Search backend: auto, bing, brave, duckduckgo, google, wikipedia, etc.")
) -> List[Dict[str, Any]]:
    """
    Perform web search using DDGS.

    Returns a list of search results with title, href (URL), and body (description).
    """
    try:
        logger.info(f"Search request: query='{query}', region={region}, backend={backend}, page={page}")

        results = DDGS().text(
            query=query,
            region=region,
            safesearch=safesearch,
            timelimit=timelimit,
            max_results=max_results,
            page=page,
            backend=backend
        )

        results_list = list(results)

        # Filter out grokipedia results
        # Nope. We are NOT using that!
        # We are NOT poisoning Golem with Musk garbage.
        filtered_results = [
            result for result in results_list
            if 'grokipedia' not in result.get('href', '').lower()
        ]

        logger.info(f"Search completed: found {len(results_list)} results ({len(filtered_results)} after filtering)")

        return filtered_results

    except Exception as e:
        logger.error(f"Search error: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"Search failed: {str(e)}"
        )


if __name__ == "__main__":
    import uvicorn

    logger.info("Starting DDGS Search Service...")
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8001,
        log_level="info"
    )
