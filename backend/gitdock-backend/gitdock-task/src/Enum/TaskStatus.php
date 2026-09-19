<?php
namespace App\Enum;

enum TaskStatus: string
{
    case TODO = 'todo';
    case IN_PROGRESS = 'in_progress';
    case IN_REVIEW = 'in_review';
    case TESTING = 'testing';
    case DONE = 'done';
    case CANCELED = 'canceled';
}
