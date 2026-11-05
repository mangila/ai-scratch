# {{pokemon.name}} (National Dex #{{pokemon.id}}) | Species ID: #{{species.id}}

- **Types:** {{#pokemon.types}}{{type.name}} (Slot: {{slot}}) {{/pokemon.types}}
- **Generation:** {{species.generation.name}}
- **Habitat:** {{species.habitat.name}}
- **Capture Rate:** {{species.capture_rate}}

## Description by Game

{{#species.flavor_text_entries}}
{{#is_english}}
> * **({{version.name}} –** {{flavor_text}}
    {{/is_english}}
    {{/species.flavor_text_entries}}

---

## {{pokemon.name}} (ID: {{pokemon.id}}) - Base Stats

- **Base Experience:** {{pokemon.base_experience}}
  {{#pokemon.stats}}
- **{{stat.name}}:** {{base_stat}}
  {{/pokemon.stats}}

---

## {{pokemon.name}} (ID: {{pokemon.id}}) - Abilities & Physical Traits

- **Height:** {{pokemon.height}} decimetres
- **Weight:** {{pokemon.weight}} hectograms
- **Abilities:**
  {{#pokemon.abilities}}
    - **{{ability.name}} {{#is_hidden}} (Hidden Ability){{/is_hidden}}
  {{/pokemon.abilities}}

---

## {{pokemon.name}} (ID: {{pokemon.id}}) - Media Assets

- Official Artwork URL: {{pokemon.sprites.other.official-artwork.front_default}}
- Default Sprite URL: {{pokemon.sprites.front_default}}