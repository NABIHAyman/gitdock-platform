<?php

namespace App\Tests\Service;

use App\Service\SmartCloseService;
use PHPUnit\Framework\TestCase;

class SmartCloseServiceTest extends TestCase
{
    public function testExtractsTaskIdFromFixesReference(): void
    {
        $this->assertSame(24, SmartCloseService::extractTaskId('Fixes #24 - validation du formulaire'));
    }

    public function testIsCaseInsensitiveAndToleratesSpaces(): void
    {
        $this->assertSame(7, SmartCloseService::extractTaskId('refactor: parser (fixes   #7)'));
    }

    public function testReturnsNullWithoutFixesReference(): void
    {
        $this->assertNull(SmartCloseService::extractTaskId('feat: ajout du tableau de bord #12'));
        $this->assertNull(SmartCloseService::extractTaskId('Fixes the login bug'));
    }
}
